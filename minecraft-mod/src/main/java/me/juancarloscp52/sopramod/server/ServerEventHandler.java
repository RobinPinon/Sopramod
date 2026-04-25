/*
 * Copyright (c) 2021 juancarloscp52
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

package com.poc.sopramod.server;

import com.poc.sopramod.Sopramod;
import com.poc.sopramod.SopramodSettings;
import com.poc.sopramod.Variables;
import com.poc.sopramod.events.Event;
import com.poc.sopramod.events.EventRegistry;
import com.poc.sopramod.events.EventType;
import com.poc.sopramod.networking.ClientboundAddEvent;
import com.poc.sopramod.networking.ClientboundRemoveFirst;
import com.poc.sopramod.networking.ClientboundTick;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ServerEventHandler {

    private final SopramodSettings settings = Sopramod.getInstance().settings;
    public List<Event> currentEvents = new ArrayList<>();
    public MinecraftServer server;
    public VotingServer voting;
    private boolean started = false;
    private short eventCountDown;


    public void init(MinecraftServer server) {
        resetTimer();
        this.server = server;

        if (settings.integrations) {
            voting = new VotingServer();
            voting.enable();
        }

        this.started = true;
    }

    public void tick(boolean noNewEvents) {

        if (!this.started)
            return;

        //Reset timer if countdown is larger than timer duration. This prevents errors while manually executing timer speed events.
        if(eventCountDown>settings.timerDuration/Variables.timerMultiplier)
            resetTimer();


        if (eventCountDown == 0) {

            if (currentEvents.size() > 3) {

                if (currentEvents.get(0).hasEnded()) {
                    PlayerLookup.all(server).forEach(serverPlayerEntity ->
                            ServerPlayNetworking.send(serverPlayerEntity, ClientboundRemoveFirst.INSTANCE));

                    currentEvents.remove(0);
                }
            }


            if (!noNewEvents) {
                // Get next Event from chat votes (if enabled) or randomly
                Optional<Event> event;
                if (settings.integrations) {
                    if (voting.events.isEmpty()) {
                        Sopramod.LOGGER.info("[Chat Integrations] No random event available");
                        event = Optional.empty();
                    } else {
                        int winner = voting.getWinner();
                        if (winner == -1 || winner == 3)    // -1 - no winner, 3 - Random Event : Get Random Event.
                            event = EventRegistry.getRandomDifferentEvent(currentEvents).map(Holder::value).map(EventType::create);
                        else    // Get winner
                            event = Optional.of(voting.events.get(winner));
                        if (event.isPresent()) {
                            Sopramod.LOGGER.info("[Chat Integrations] Winner event: {}", event.get().getDescription().getString());
                        } else {
                            Sopramod.LOGGER.info("[Chat Integrations] No selectable event");
                        }
                    }
                } else {
                    event = EventRegistry.getRandomDifferentEvent(currentEvents).map(Holder::value).map(EventType::create);
                }

                event.ifPresent(this::runEvent);
                if (settings.integrations)
                    voting.newPoll();

                // Reset timer.
                resetTimer();
            }
        }

        // Tick all events.
        for (Event event : currentEvents) {
            if (!event.hasEnded())
                event.tick();
        }

        // Send tick to clients.
        final ClientboundTick tick = new ClientboundTick(eventCountDown);
        PlayerLookup.all(server).forEach(serverPlayerEntity ->
                ServerPlayNetworking.send(serverPlayerEntity, tick));


        eventCountDown--;
    }

    public boolean runEvent(Event event) {
        if (event == null) {
            Sopramod.LOGGER.info("New Event not found");
            return false;
        }

        final FeatureFlagSet featureFlagSet = event.getType().requiredFeatures();
        if (!featureFlagSet.isSubsetOf(server.overworld().enabledFeatures())) {
            final Optional<String> missing = FeatureFlags.REGISTRY.toNames(featureFlagSet.subtract(server.overworld().enabledFeatures())).stream().map(Identifier::toString).reduce((s, t) -> s + ", " + t);
            Sopramod.LOGGER.info("Tried to run event that requires disabled features, missing: {}", missing.orElse("unknown"));
            return false;
        }

        Sopramod.LOGGER.info("New Event: {} total duration: {}", event.getDescription().getString(), event.getDuration());
        // Start the event and add it to the list.
        event.init();
        currentEvents.add(event);

        sendEventToPlayers(event);
        return true;
    }

    private void sendEventToPlayers(Event event) {
        PlayerLookup.all(server).forEach(serverPlayerEntity ->
                ServerPlayNetworking.send(serverPlayerEntity, new ClientboundAddEvent(event)));
    }

    public void endChaos() {
        if (voting != null)
            voting.disable();

        currentEvents.forEach(Event::end);
    }

    public void endChaosPlayer(ServerPlayer player) {
        currentEvents.forEach(event -> {
            if (!event.hasEnded())
                event.endPlayer(player);
        });
    }

    public void resetTimer(){
        eventCountDown = (short) (settings.timerDuration/Variables.timerMultiplier);
    }


    public List<ServerPlayer> getActivePlayers(){
        return PlayerLookup.all(Sopramod.getInstance().eventHandler.server).stream().filter(serverPlayerEntity -> !serverPlayerEntity.isSpectator()).collect(Collectors.toList());
    }
}
