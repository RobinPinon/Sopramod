package com.poc.sopramod.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.poc.sopramod.Sopramod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PlayerHeartStorage {

    private static final double MIN_MAX_HEALTH = 2.0;
    /** Ancien emplacement global (avant persistance par monde). */
    private static final Path LEGACY_GLOBAL_FILE = Path.of("config", "sopramod", "player_hearts.json");
    private static final Type STORAGE_TYPE = new TypeToken<Map<String, Double>>() {}.getType();

    private final Gson gson = new Gson();
    private Path storageFile = null;
    private final Map<String, Double> maxHealthByPlayerId = new HashMap<>();

    /**
     * Lie le stockage au dossier de sauvegarde du monde (ex. {@code saves/<monde>/sopramod/player_hearts.json}).
     */
    public synchronized void bindToWorld(Path worldRoot) {
        Path dir = worldRoot.resolve("sopramod");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            Sopramod.LOGGER.warn("Impossible de créer le dossier sopramod dans la sauvegarde : {}", e.getLocalizedMessage());
        }
        this.storageFile = dir.resolve("player_hearts.json");
        maxHealthByPlayerId.clear();
        loadFromDisk();
        migrateLegacyGlobalIfNeeded();
    }

    private void loadFromDisk() {
        if (storageFile == null || !Files.isRegularFile(storageFile)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(storageFile)) {
            Map<String, Double> loaded = gson.fromJson(reader, STORAGE_TYPE);
            if (loaded != null) {
                maxHealthByPlayerId.putAll(loaded);
            }
        } catch (IOException e) {
            Sopramod.LOGGER.warn("Impossible de charger player_hearts.json (monde) : {}", e.getLocalizedMessage());
        }
    }

    /**
     * Si ce monde n’a pas encore de fichier, importe une fois l’ancien fichier global s’il existe.
     */
    private void migrateLegacyGlobalIfNeeded() {
        if (storageFile == null || !Files.isRegularFile(storageFile)) {
            File legacy = LEGACY_GLOBAL_FILE.toFile();
            if (!legacy.exists()) {
                return;
            }
            try (FileReader reader = new FileReader(legacy)) {
                Map<String, Double> loaded = gson.fromJson(reader, STORAGE_TYPE);
                if (loaded != null && !loaded.isEmpty()) {
                    maxHealthByPlayerId.putAll(loaded);
                    save();
                    Sopramod.LOGGER.info("Sopramod : cœurs max importés depuis {} vers la sauvegarde du monde.", LEGACY_GLOBAL_FILE);
                }
            } catch (IOException e) {
                Sopramod.LOGGER.warn("Migration player_hearts (global → monde) échouée : {}", e.getLocalizedMessage());
            }
        }
    }

    public synchronized void applySavedOrRememberCurrent(ServerPlayer player) {
        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr == null) {
            return;
        }
        String playerId = player.getStringUUID();
        Double savedMaxHealth = maxHealthByPlayerId.get(playerId);
        if (savedMaxHealth == null) {
            maxHealthByPlayerId.put(playerId, maxHealthAttr.getBaseValue());
            save();
            return;
        }
        setPlayerMaxHealth(player, maxHealthAttr, savedMaxHealth);
    }

    public synchronized void setAndSave(ServerPlayer player, double targetMaxHealth) {
        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr == null) {
            return;
        }
        double applied = setPlayerMaxHealth(player, maxHealthAttr, targetMaxHealth);
        maxHealthByPlayerId.put(player.getStringUUID(), applied);
        save();
    }

    private double setPlayerMaxHealth(ServerPlayer player, AttributeInstance maxHealthAttr, double targetMaxHealth) {
        double clampedMaxHealth = Math.max(MIN_MAX_HEALTH, targetMaxHealth);
        maxHealthAttr.setBaseValue(clampedMaxHealth);
        if (player.getHealth() > clampedMaxHealth) {
            player.setHealth((float) clampedMaxHealth);
        }
        return clampedMaxHealth;
    }

    private synchronized void save() {
        if (storageFile == null) {
            Sopramod.LOGGER.warn("Sopramod : sauvegarde des cœurs ignorée (monde non encore lié).");
            return;
        }
        try {
            Files.createDirectories(storageFile.getParent());
            Files.writeString(storageFile, gson.toJson(maxHealthByPlayerId));
        } catch (IOException e) {
            Sopramod.LOGGER.warn("Impossible d'enregistrer player_hearts.json (monde) : {}", e.getLocalizedMessage());
        }
    }
}
