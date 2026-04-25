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
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class ServerEventHandler {

    private final SopramodSettings settings = Sopramod.getInstance().settings;
    public List<Event> currentEvents = new ArrayList<>();
    public MinecraftServer server;
    public VotingServer voting;
    private boolean started = false;
    private short eventCountDown;
    private final ConcurrentLinkedQueue<ForcedEventRequest> forcedEvents = new ConcurrentLinkedQueue<>();

    private record ForcedEventRequest(String eventId, String triggeredBy) {}
    public record ForcedEventResult(boolean ok, String code, String eventId, String label) {}


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

        processForcedEvents();

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

    private void processForcedEvents() {
        ForcedEventRequest req;
        while ((req = forcedEvents.poll()) != null) {
            String requested = normalizeForcedEventId(req.eventId());
            Optional<EventType<?>> eventType = EventRegistry.EVENTS.listElements()
                .map(Holder::value)
                .filter(type -> normalizeForcedEventId(EventRegistry.getEventId(type).identifier().getPath()).equals(requested))
                .findFirst();

            if (eventType.isEmpty()) {
                Sopramod.LOGGER.warn("Forced event ignored, unknown id: {}", req.eventId());
                continue;
            }

            Event event = eventType.get().create();
            String by = (req.triggeredBy() == null || req.triggeredBy().isBlank()) ? "killer" : req.triggeredBy();
            if (runEvent(event, by)) {
                resetTimer();
                Sopramod.LOGGER.info("Forced event executed: {} by {}", req.eventId(), by.toLowerCase(Locale.ROOT));
            }
        }
    }

    public boolean runEvent(Event event) {
        return runEvent(event, "");
    }

    public boolean runEvent(Event event, String triggeredBy) {
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

        sendEventToPlayers(event, triggeredBy);
        return true;
    }

    private void sendEventToPlayers(Event event, String triggeredBy) {
        String displayName = triggeredBy == null ? "" : triggeredBy.trim();
        PlayerLookup.all(server).forEach(serverPlayerEntity ->
                ServerPlayNetworking.send(serverPlayerEntity, new ClientboundAddEvent(event, displayName)));
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

    public void enqueueForcedEvent(String eventId, String triggeredBy) {
        if (eventId == null || eventId.isBlank()) {
            return;
        }
        forcedEvents.add(new ForcedEventRequest(eventId.trim(), triggeredBy));
    }

    public ForcedEventResult forceEventNow(String eventId, String triggeredBy) {
        if (eventId == null || eventId.isBlank()) {
            return new ForcedEventResult(false, "missing_event", "", "");
        }

        String requested = normalizeForcedEventId(eventId);
        Optional<EventType<?>> eventType = EventRegistry.EVENTS.listElements()
            .map(Holder::value)
            .filter(type -> normalizeForcedEventId(EventRegistry.getEventId(type).identifier().getPath()).equals(requested))
            .findFirst();

        if (eventType.isEmpty()) {
            return new ForcedEventResult(false, "unknown_event", eventId, "");
        }

        Event event = eventType.get().create();
        String by = (triggeredBy == null || triggeredBy.isBlank()) ? "killer" : triggeredBy;
        if (!runEvent(event, by)) {
            return new ForcedEventResult(false, "event_rejected", eventId, "");
        }

        resetTimer();
        String label = event.getDescription().getString();
        Sopramod.LOGGER.info("Forced event executed now: {} by {}", eventId, by.toLowerCase(Locale.ROOT));
        return new ForcedEventResult(true, "ok", eventId, label);
    }

    private static String normalizeForcedEventId(String value) {
        return value
            .trim()
            .toLowerCase(Locale.ROOT)
            .replace(" ", "_")
            .replace("-", "_")
            .replace("'", "")
            .replace("!", "")
            .replace("(", "")
            .replace(")", "");
    }


    public List<ServerPlayer> getActivePlayers(){
        return PlayerLookup.all(Sopramod.getInstance().eventHandler.server).stream().filter(serverPlayerEntity -> !serverPlayerEntity.isSpectator()).collect(Collectors.toList());
    }
}
