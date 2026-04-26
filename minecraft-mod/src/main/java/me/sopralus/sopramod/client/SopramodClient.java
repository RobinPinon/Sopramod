/*
 * Copyright (c) 2026 sopralus
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.poc.sopramod.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.JsonOps;
import com.poc.sopramod.Sopramod;
import com.poc.sopramod.events.Event;
import com.poc.sopramod.events.EventType;
import com.poc.sopramod.mixin.FogRendererAccessor;
import com.poc.sopramod.mixin.GameRendererAccessor;
import com.poc.sopramod.networking.ClientboundJoinSync;
import com.poc.sopramod.networking.NetworkingConstants;
import com.poc.sopramod.networking.ServerboundJoinHandshake;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class SopramodClient implements ClientModInitializer {

    public static final Logger LOGGER = LogManager.getLogger();
    public static final Identifier herobrineAmbienceID = Identifier.fromNamespaceAndPath("sopramod", "ambient.herobrine");
    public static SopramodClient instance;
    public static SoundEvent herobrineAmbience = SoundEvent.createVariableRangeEvent(herobrineAmbienceID);
    public ClientEventHandler clientEventHandler;
    public SopramodIntegrationsSettings integrationsSettings;

    /**
     * Laisse vides pour utiliser uniquement le fichier
     * {@code config/sopramod/sopramodIntegrationSettings.json}. Sinon, remplis token + chaîne ici :
     * ces valeurs remplacent le JSON au chargement (utile si la sauvegarde via le menu ne marche pas).
     * Ne commite pas un vrai token — préfère un remplacement local.
     */
    private static final String HARDCODE_TWITCH_OAUTH = "jizlr0igy4xy2fnnfdxiwiemyp21qt";
    private static final String HARDCODE_TWITCH_CHANNEL = "sopralus";
    /**
     * Login Twitch (minuscule) du compte qui a généré le token — requis sur l’IRC comme NICK.
     * Laisser vide pour utiliser le même nom que le channel (cas streamer = même compte sur sa chaîne).
     * Remplir si le token vient d’un compte bot différent de la chaîne cible.
     */
    private static final String HARDCODE_TWITCH_IRC_LOGIN = "sopralus";

    public static SopramodClient getInstance() {
        return instance;
    }

    /**
     * NICK IRC autorisé par Twitch = login du compte associé au token, pas (forcément) le nom de la chaîne cible.
     */
    public static String resolveTwitchIrcLogin() {
        SopramodClient inst = getInstance();
        if (inst == null || inst.integrationsSettings == null) {
            return "";
        }
        String irc = HARDCODE_TWITCH_IRC_LOGIN == null ? "" : HARDCODE_TWITCH_IRC_LOGIN.trim();
        if (!irc.isEmpty()) {
            return irc.toLowerCase();
        }
        return inst.integrationsSettings.twitch.channel == null ? "" : inst.integrationsSettings.twitch.channel.toLowerCase();
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initialisation du client Sopramod");
        instance = this;
        loadSettings();
        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.JOIN_CONFIRM, (confirm, context) -> {
            clientEventHandler = new ClientEventHandler(confirm.timerDuration(), confirm.baseEventDuration(), confirm.integrations());
        });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.JOIN_SYNC, (sync, context) -> {
            if (clientEventHandler == null)
                return;

            if (sync.events().size() == clientEventHandler.currentEvents.size())
                return;
            for (final ClientboundJoinSync.EventData data : sync.events()) {
                Event event = data.event();
                EventType<?> type = event.getType();
                event.setEnded(data.ended());
                event.setTickCount(data.tickCount());
                if (data.tickCount() > 0 && !data.ended() && type.isEnabled())
                    event.initClient();
                context.client().execute(() -> clientEventHandler.currentEvents.add(event));
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.TICK, (tick, context) -> {
            if (clientEventHandler == null)
                return;
            context.client().execute(() -> clientEventHandler.tick(tick.eventCountDown()));
        });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.REMOVE_FIRST, (removeFirst, context) -> {
            if (clientEventHandler == null)
                return;
            context.client().execute(() -> {
                if (!clientEventHandler.currentEvents.isEmpty())
                    clientEventHandler.remove((byte) 0);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.REMOVE_ENDED, (removeEnded, context) -> {
            if (clientEventHandler == null)
                return;
            context.client().execute(() -> {
                if (!clientEventHandler.currentEvents.isEmpty())
                    clientEventHandler.currentEvents.removeIf(Event::hasEnded);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.ADD_EVENT, (addEvent, context) -> {
            if (clientEventHandler == null)
                return;
            context.client().execute(() -> {
                clientEventHandler.addEvent(addEvent.event(), addEvent.triggeredBy());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.END_EVENT, (endEvent, context) -> {
            if (clientEventHandler == null)
                return;
            context.client().execute(() -> {
                Event event = clientEventHandler.currentEvents.get(endEvent.index());
                if(event != null)
                    event.endClient();
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.NEW_POLL, (newPoll, context) -> {
            if (clientEventHandler == null || clientEventHandler.votingClient == null)
                return;
            context.client().execute(() -> clientEventHandler.votingClient.newPoll(newPoll.voteId(), newPoll.events()));
        });

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.POLL_STATUS, (pollStatus, context) -> {
            if (clientEventHandler == null || clientEventHandler.votingClient == null)
                return;
            context.client().execute(() -> clientEventHandler.votingClient.updatePollStatus(pollStatus.voteId(), pollStatus.totalVotes(), pollStatus.totalVotesCount()));
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (clientEventHandler == null)
                return;
            clientEventHandler.endChaos();
            clientEventHandler = null;
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            final Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer("sopramod");
            if (modContainer.isPresent()) {
                final String version = modContainer.get().getMetadata().getVersion().getFriendlyString();
                ClientPlayNetworking.send(new ServerboundJoinHandshake(version));
            }
        });

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("sopramod", "overlay"), (drawContext, tickCounter) -> {
            if (clientEventHandler != null)
                clientEventHandler.render(drawContext, tickCounter);
        });

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            GameRendererAccessor gameRenderer = (GameRendererAccessor) client.gameRenderer;
            List<FogEnvironment> fogEnvironments = ((FogRendererAccessor) gameRenderer.getFogRenderer()).getFogEnvironments();
            fogEnvironments.addFirst(new HerobrineFogEnvironment());
            fogEnvironments.add(1, new RainbowFogEnvironment());
        });

        //Registry.registerReference()
        Registry.register(BuiltInRegistries.SOUND_EVENT, herobrineAmbienceID, herobrineAmbience);
        ParticleFactoryRegistry.getInstance().register(Sopramod.CONSTANT_COLOR_DUST, ConstantColorDustParticle.Factory::new);
    }

    public void loadSettings() {
        File file = new File("./config/sopramod/sopramodIntegrationSettings.json");
        if (file.exists()) {
            try {
                FileReader fileReader = new FileReader(file);
                final JsonObject json = GsonHelper.parse(fileReader);
                fileReader.close();
                if (json.has("integrationType")) {
                    convertToNewFormat(json);
                }
                integrationsSettings = SopramodIntegrationsSettings.CODEC.parse(JsonOps.INSTANCE, json).result().orElseGet(SopramodIntegrationsSettings::new);
            } catch (IOException e) {
                LOGGER.warn("Impossible de charger les paramètres d'intégration Sopramod : {}", e.getLocalizedMessage());
            }
        } else {
            integrationsSettings = new SopramodIntegrationsSettings();
            saveSettings();
        }
        applyHardcodedTwitchFromSource();
    }

    private void applyHardcodedTwitchFromSource() {
        if (integrationsSettings == null) {
            return;
        }
        String t = HARDCODE_TWITCH_OAUTH == null ? "" : HARDCODE_TWITCH_OAUTH.trim();
        String c = HARDCODE_TWITCH_CHANNEL == null ? "" : HARDCODE_TWITCH_CHANNEL.trim();
        if (t.isEmpty() || c.isEmpty()) {
            return;
        }
        integrationsSettings.twitch.token = t;
        integrationsSettings.twitch.channel = c.toLowerCase();
        integrationsSettings.twitch.enabled = true;
    }

    private void convertToNewFormat(final JsonObject json) {
        final int integrationType = json.remove("integrationType").getAsInt();
        convert(json, "sendChatMessages", json, "send_chat_messages");
        convert(json, "showCurrentPercentage", json, "show_current_percentage");
        convert(json, "showUpcomingEvents", json, "show_upcoming_events");

        final JsonObject twitch = new JsonObject();
        convert(json, "authToken", twitch, "token");
        convert(json, "channel", twitch, "channel");
        twitch.add("enabled", new JsonPrimitive(integrationType == 1));
        json.add("twitch", twitch);

        final JsonObject discord = new JsonObject();
        convert(json, "discordToken", discord, "token");
        convert(json, "discordChannel", discord, "channel");
        discord.add("enabled", new JsonPrimitive(integrationType == 2));
        json.add("discord", discord);

        final JsonObject youtube = new JsonObject();
        convert(json, "youtubeClientId", youtube, "client_id");
        convert(json, "youtubeSecret", youtube, "secret");
        convert(json, "youtubeAccessToken", youtube, "access_token");
        convert(json, "youtubeRefreshToken", youtube, "refresh_token");
        youtube.add("enabled", new JsonPrimitive(integrationType == 3));
        json.add("youtube", youtube);
    }

    private void convert(final JsonObject source, final String sourceKey, final JsonObject target, final String targetKey) {
        final JsonElement contents = source.remove(sourceKey);
        if (contents != null) {
            target.add(targetKey, contents);
        }
    }

    public void saveSettings() {
        File file = new File("./config/sopramod/sopramodIntegrationSettings.json");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            final JsonElement json = SopramodIntegrationsSettings.CODEC.encodeStart(JsonOps.INSTANCE, integrationsSettings).getOrThrow();
            FileWriter fileWriter = new FileWriter(file);
            JsonWriter jsonWriter = new JsonWriter(fileWriter);
            jsonWriter.setSerializeNulls(false);
            jsonWriter.setIndent("    ");
            GsonHelper.writeValue(jsonWriter, json, null);
            fileWriter.close();
        } catch (IOException e) {
            LOGGER.warn("Impossible d'enregistrer les paramètres d'intégration Sopramod : {}", e.getLocalizedMessage());
        }
    }

}
