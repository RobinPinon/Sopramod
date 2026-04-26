package com.poc.sopramod.events.db;

import com.poc.sopramod.Sopramod;
import com.poc.sopramod.events.AbstractInstantEvent;
import com.poc.sopramod.events.EventType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Efface la sauvegarde dans {@link net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents#SERVER_STOPPED}.
 * En solo, on ne doit pas appeler {@link MinecraftServer#halt(boolean)} : la déconnexion du joueur arrête déjà
 * le serveur intégré ; un double arrêt bloquait l’écran « Sauvegarde du monde ».
 */
public class OnRecommenceEvent extends AbstractInstantEvent {
    public static final EventType<OnRecommenceEvent> TYPE = EventType.builder(OnRecommenceEvent::new).build();

    @Override
    public void init() {
        MinecraftServer server = Sopramod.resolveServerForChaos();
        if (server == null) {
            Sopramod.LOGGER.warn("Sopramod: on_recommence — aucun MinecraftServer (eventHandler jamais initialisé ?).");
            return;
        }
        Path worldRoot = Sopramod.resolveWorldRootPath(server);
        if (worldRoot == null) {
            Sopramod.LOGGER.error("Sopramod: on_recommence — impossible de déterminer le dossier de la map.");
            return;
        }
        Sopramod.LOGGER.info("Sopramod: on_recommence — la sauvegarde {} sera supprimée après arrêt complet du serveur.", worldRoot);
        Sopramod.worldDeletePath = worldRoot;
        Sopramod.pendingWorldDelete = true;
        Component msg = Component.translatable("sopramod.world_reset.kick");
        List<ServerPlayer> copy = new ArrayList<>(server.getPlayerList().getPlayers());
        for (ServerPlayer player : copy) {
            player.connection.disconnect(msg);
        }
        server.saveEverything(true, true, true);
        if (server.isSingleplayer()) {
            Sopramod.LOGGER.info("Sopramod: on_recommence — solo: pas de halt(), le serveur intégré s’arrête avec la déconnexion.");
            return;
        }
        server.execute(() -> server.halt(true));
    }

    @Override
    public EventType<OnRecommenceEvent> getType() {
        return TYPE;
    }
}
