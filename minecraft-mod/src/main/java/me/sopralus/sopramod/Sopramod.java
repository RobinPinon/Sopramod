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

package com.poc.sopramod;

import com.google.gson.Gson;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.poc.sopramod.events.Event;
import com.poc.sopramod.events.EventRegistry;
import com.poc.sopramod.events.EventType;
import com.poc.sopramod.events.db.StutteringEvent;
import com.poc.sopramod.networking.ClientboundJoinConfirm;
import com.poc.sopramod.networking.ClientboundJoinSync;
import com.poc.sopramod.networking.ClientboundRemoveEnded;
import com.poc.sopramod.networking.NetworkingConstants;
import com.poc.sopramod.server.ConstantColorDustParticleOptions;
import com.poc.sopramod.server.LocalOverrideHttpServer;
import com.poc.sopramod.server.ServerEventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Sopramod implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static Sopramod instance;
    public ServerEventHandler eventHandler;
    public SopramodSettings settings;
    private final LocalOverrideHttpServer localOverrideHttpServer = new LocalOverrideHttpServer();
    public static final ParticleType<ConstantColorDustParticleOptions> CONSTANT_COLOR_DUST = FabricParticleTypes.complex(false, ConstantColorDustParticleOptions.CODEC, ConstantColorDustParticleOptions.PACKET_CODEC);

    private static final DynamicCommandExceptionType ERROR_INVALID_ON_CLIENT = new DynamicCommandExceptionType(eventId -> Component.translatable("sopramod.command.invalidClientSide", eventId));
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_EVENT = new DynamicCommandExceptionType(eventId -> Component.translatable("sopramod.command.unknownEvent", eventId));

    public static Sopramod getInstance() {
        return instance;
    }

    @Override
    public void onInitialize() {
        instance = this;
        loadSettings();
        LOGGER.info("Sopramod Started");
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath("sopramod", "constant_color_dust"), CONSTANT_COLOR_DUST);
        localOverrideHttpServer.start();

        ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.JOIN_HANDSHAKE, (handshake, context) -> {
            String version = FabricLoader.getInstance().getModContainer("sopramod").get().getMetadata().getVersion().getFriendlyString();
            if (version.equals(handshake.clientVersion())) {
                final ClientboundJoinConfirm confirm = new ClientboundJoinConfirm(settings.timerDuration, settings.baseEventDuration, settings.integrations);
                final ServerPlayer player = context.player();
                ServerPlayNetworking.send(player, confirm);
                if (PlayerLookup.all(context.server()).size() == 1) {
                    eventHandler = new ServerEventHandler();
                    eventHandler.init(context.server());
                }

                List<Event> currentEvents = eventHandler.currentEvents;
                if (!currentEvents.isEmpty()) {
                    ClientboundJoinSync sync = new ClientboundJoinSync(currentEvents.stream().map(currentEvent -> new ClientboundJoinSync.EventData(
                        currentEvent,
                        currentEvent.hasEnded(),
                        currentEvent.getTickCount()
                    )).toList());
                    ServerPlayNetworking.send(player, sync);
                }

                if (settings.integrations && eventHandler.voting != null) {
                    eventHandler.voting.sendNewPollToPlayer(player);
                }
            } else {
                LOGGER.warn(String.format("Player %s (%s) sopramod version (%s) does not match server sopramod version (%s). Kicking...", context.player().getName(), context.player().getStringUUID(), handshake.clientVersion(), version));
                context.player().connection.disconnect(Component.literal(String.format("Client sopramod version (%s) does not match server version (%s).", handshake.clientVersion(), version)));
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (eventHandler == null)
                return;
            eventHandler.endChaosPlayer(handler.player);
            if (PlayerLookup.all(server).size() <= 1) {
                eventHandler.endChaos();
                eventHandler = null;
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.VOTES, (votes, context) -> {
            if (eventHandler == null || eventHandler.voting == null)
                return;
            context.server().execute(() -> eventHandler.voting.receiveVotes(votes.voteId(), votes.votes()));
        });

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (eventHandler != null)
                eventHandler.tick(false);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (eventHandler != null)
                eventHandler.endChaos();
            localOverrideHttpServer.stop();
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(LiteralArgumentBuilder.<CommandSourceStack>literal("sopramod")
                    .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                    .then(Commands.literal("clearPastEvents")
                            .executes(source -> {
                                ServerEventHandler eventHandler = Sopramod.getInstance().eventHandler;

                                eventHandler.currentEvents.removeIf(Event::hasEnded);
                                PlayerLookup.all(eventHandler.server).forEach(player -> ServerPlayNetworking.send(player, ClientboundRemoveEnded.INSTANCE));
                                return 0;
                            }))
                    .then(Commands.literal("run")
                            .then(Commands.argument("event", ResourceArgument.resource(registryAccess, EventRegistry.REGISTRY_KEY))
                                    .suggests((context, builder) ->
                                            SharedSuggestionProvider.suggestResource(
                                                EventRegistry.EVENTS.stream().filter(type -> type.hasRequiredFeatures(context.getSource().enabledFeatures())),
                                                builder,
                                                type -> EventRegistry.getEventId(type).identifier(),
                                                type -> Component.translatable(type.getLanguageKey())
                                            )
                                    )
                                    .executes(source -> {
                                        ServerEventHandler eventHandler = Sopramod.getInstance().eventHandler;

                                        if(eventHandler != null) {
                                            Holder.Reference<EventType<?>> event = ResourceArgument.getResource(source, "event", EventRegistry.REGISTRY_KEY);

                                            // If running on integrated server, prevent running Stuttering event.
                                            if(FabricLoader.getInstance().getEnvironmentType() != EnvType.SERVER && event.value().equals(StutteringEvent.TYPE)){
                                                throw ERROR_INVALID_ON_CLIENT.create(event.getRegisteredName());
                                            }

                                            if(eventHandler.runEvent(event.value().create()))
                                                Sopramod.LOGGER.warn("New event run via command: {}", event.getRegisteredName());
                                            else
                                                throw ERROR_UNKNOWN_EVENT.create(event.getRegisteredName());
                                        }

                                        return 0;
                                    }))));
        });
    }


    public void loadSettings() {
        File file = new File("./config/sopramod/sopramod.json");
        Gson gson = new Gson();
        if (file.exists()) {
            try {
                FileReader fileReader = new FileReader(file);
                settings = gson.fromJson(fileReader, SopramodSettings.class);
                fileReader.close();
            } catch (IOException e) {
                LOGGER.warn("Could not load sopramod settings: " + e.getLocalizedMessage());
            }
        } else {
            settings = new SopramodSettings();
            saveSettings();
        }
    }

    public void saveSettings() {
        Gson gson = new Gson();
        File file = new File("./config/sopramod/sopramod.json");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(gson.toJson(settings));
            fileWriter.close();
        } catch (IOException e) {
            LOGGER.warn("Could not save sopramod settings: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}
