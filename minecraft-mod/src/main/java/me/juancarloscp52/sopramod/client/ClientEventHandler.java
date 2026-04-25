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

package com.poc.sopramod.client;

import com.poc.sopramod.Sopramod;
import com.poc.sopramod.SopramodSettings.UIStyle;
import com.poc.sopramod.Variables;
import com.poc.sopramod.client.UIStyles.GTAVUIRenderer;
import com.poc.sopramod.client.UIStyles.MinecraftUIRenderer;
import com.poc.sopramod.client.UIStyles.UIRenderer;
import com.poc.sopramod.client.integrations.Integration;
import com.poc.sopramod.client.integrations.IntegrationType;
import com.poc.sopramod.events.Event;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientEventHandler {


    public List<Event> currentEvents = new ArrayList<>();
    public VotingClient votingClient;
    public Minecraft client;
    public short eventCountDown;

    short timerDuration;
    UIRenderer renderer = null;
    final short timerDurationFinal;
    boolean serverIntegrations;

    public ClientEventHandler(short timerDuration, short baseEventDuration, boolean enableIntegrations) {
        this.client = Minecraft.getInstance();
        this.timerDuration = timerDuration;
        this.timerDurationFinal = timerDuration;
        this.eventCountDown = timerDuration;
        this.serverIntegrations = enableIntegrations;

        Sopramod.getInstance().settings.baseEventDuration = baseEventDuration;

        if (Sopramod.getInstance().settings.integrations && enableIntegrations) {
            votingClient = new VotingClient();
            final SopramodIntegrationsSettings integrationsSettings = SopramodClient.getInstance().integrationsSettings;
            final List<Integration> integrations = Arrays.stream(IntegrationType.values())
                .filter(type -> type.settings(integrationsSettings).enabled())
                .map(type -> type.create(this, votingClient))
                .toList();
            votingClient.setIntegrations(integrations);
            votingClient.enable();
        }

        if(Sopramod.getInstance().settings.UIstyle == UIStyle.MINECRAFT){
            renderer = new MinecraftUIRenderer();
        }
        else if (Sopramod.getInstance().settings.UIstyle == UIStyle.GTAV) {
            renderer = new GTAVUIRenderer(votingClient);
        }

    }

    public void tick(short eventCountDown) {
        this.timerDuration= (short) (timerDurationFinal/Variables.timerMultiplier);
        this.eventCountDown = eventCountDown;

        if (eventCountDown % 10 == 0 && votingClient != null) {
            votingClient.sendVotes();
        }
        if(!client.player.isSpectator()) {
            for (Event event : currentEvents) {
                if (!event.hasEnded())
                    event.tickClient();
            }
        }
    }

    public void render(GuiGraphics drawContext, DeltaTracker tickCounter) {
        // Render active event effects
        currentEvents.forEach(event -> {
            if (!event.hasEnded() && !client.player.isSpectator())
                event.render(drawContext, tickCounter);
        });

        Minecraft client = Minecraft.getInstance();

        if (client.debugEntries.isOverlayVisible())

            return;

        double time = timerDuration - eventCountDown;
        int width = client.getWindow().getGuiScaledWidth();

        // Render timer bar
        /// Only the timer is differentiated in two declination for now but
        /// it will be interesting to adapt the event queue and poll rendering too
        renderer.renderTimer(drawContext, width, time, timerDuration);

        // Render Event Queue...
        int y = 20;
        for (Event event : currentEvents) {
            if (event.alwaysShowDescription() || !Variables.doNotShowEvents) {
                event.renderQueueItem(drawContext, y);
                y += 13;
            }
        }

        // Render Poll...
        if (Sopramod.getInstance().settings.integrations && serverIntegrations && votingClient != null && votingClient.enabled) {
            votingClient.render(drawContext);
        }

    }

    public void remove(byte index) {
        currentEvents.remove(index);
    }

    public void addEvent(Event event) {
        if(!client.player.isSpectator() && event.getType().isEnabled())
            event.initClient();
        currentEvents.add(event);
    }

    public void endChaos() {

        SopramodClient.LOGGER.info("Ending events...");
        currentEvents.forEach(event -> {
            if (!event.hasEnded())
                event.endClient();
        });

        if (votingClient != null && votingClient.enabled)
            votingClient.disable();

        // Reload settings, removing downloaded settings in constructor from the server.
        SopramodClient.getInstance().loadSettings();
    }
}
