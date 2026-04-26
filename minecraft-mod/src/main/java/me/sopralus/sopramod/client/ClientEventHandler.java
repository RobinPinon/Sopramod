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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientEventHandler {

    /** Durée d'affichage de la bannière « event démarré » (toutes origines), en ticks client. */
    private static final int EVENT_ANNOUNCEMENT_TICKS = 100;
    private static final float VERTICAL_SCREEN_BORDER_RATIO = 0.341f;
    /** Taille du nom du viewer sur la bannière (sous le titre, récompense chaos / forçage). */
    private static final float EVENT_USER_NAME_SCALE = 1.15f;

    public List<Event> currentEvents = new ArrayList<>();
    public VotingClient votingClient;
    public Minecraft client;
    public short eventCountDown;

    short timerDuration;
    UIRenderer renderer = null;
    final short timerDurationFinal;
    boolean serverIntegrations;

    private Component eventAnnouncementTitle;
    private String eventAnnouncementUser;
    private int eventAnnouncementTicksLeft;

    public ClientEventHandler(short timerDuration, short baseEventDuration, boolean enableIntegrations) {
        this.client = Minecraft.getInstance();
        this.timerDuration = timerDuration;
        this.timerDurationFinal = timerDuration;
        this.eventCountDown = timerDuration;
        this.serverIntegrations = enableIntegrations;

        Sopramod.getInstance().settings.baseEventDuration = baseEventDuration;

        if (!Sopramod.getInstance().settings.integrations) {
            SopramodClient.LOGGER.warn(
                "[Sopramod] Intégrations désactivées côté serveur (config/sopramod/sopramod.json → \"integrations\": true). "
                    + "Sans cela, Twitch/Discord/YouTube ne se lancent pas.");
        } else if (!enableIntegrations) {
            SopramodClient.LOGGER.warn("[Sopramod] Intégrations non activées pour cette session (réponse serveur).");
        }
        if (Sopramod.getInstance().settings.integrations && enableIntegrations) {
            votingClient = new VotingClient();
            final SopramodIntegrationsSettings integrationsSettings = SopramodClient.getInstance().integrationsSettings;
            final List<Integration> integrations = Arrays.stream(IntegrationType.values())
                .filter(type -> type.settings(integrationsSettings).enabled())
                .map(type -> type.create(this, votingClient))
                .toList();
            if (integrations.isEmpty()) {
                SopramodClient.LOGGER.warn(
                    "[Sopramod] Aucune intégration activée dans config/sopramod/sopramodIntegrationSettings.json (ex. twitch.enabled + token + channel).");
            }
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
        if (eventAnnouncementTicksLeft > 0) {
            eventAnnouncementTicksLeft--;
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

        renderEventAnnouncementBanner(drawContext);
    }

    public void remove(byte index) {
        currentEvents.remove(index);
    }

    public void addEvent(Event event, String triggeredBy) {
        if(!client.player.isSpectator() && event.getType().isEnabled())
            event.initClient();
        eventAnnouncementTitle = event.getDescription().copy();
        eventAnnouncementUser = (triggeredBy == null || triggeredBy.isBlank()) ? null : triggeredBy.trim();
        eventAnnouncementTicksLeft = EVENT_ANNOUNCEMENT_TICKS;
        currentEvents.add(event);
    }

    private void renderEventAnnouncementBanner(GuiGraphics drawContext) {
        if (eventAnnouncementTitle == null || eventAnnouncementTicksLeft <= 0) {
            return;
        }
        Font font = client.font;
        int screenW = client.getWindow().getGuiScaledWidth();
        int screenH = client.getWindow().getGuiScaledHeight();
        float titleScale = 1.8f;
        int borderWidth = Mth.floor(screenW * VERTICAL_SCREEN_BORDER_RATIO);
        int maxOverlayWidth = Math.max(60, screenW - (borderWidth * 2));

        // Event title on top, rendered bigger.
        int titleMaxWidth = Math.max(30, (int) (maxOverlayWidth / titleScale));
        List<FormattedCharSequence> titleLines = font.split(eventAnnouncementTitle, titleMaxWidth);
        int userMaxWidth = Math.max(30, (int) (maxOverlayWidth / EVENT_USER_NAME_SCALE));
        List<FormattedCharSequence> userLines = eventAnnouncementUser == null
            ? List.of()
            : font.split(Component.literal(eventAnnouncementUser), userMaxWidth);
        int titleLineHeight = font.lineHeight + 2;
        int userLineHeight = font.lineHeight + 1;
        int titleBlockHeight = (int) (titleLines.size() * titleLineHeight * titleScale);
        int userBlockHeight = userLines.isEmpty()
            ? 0
            : (4 + (int) (userLines.size() * userLineHeight * EVENT_USER_NAME_SCALE));
        int totalBlockHeight = titleBlockHeight + userBlockHeight;
        int titleY = Math.max(8, (screenH - totalBlockHeight) / 2);
        drawContext.pose().pushMatrix();
        drawContext.pose().scale(titleScale, titleScale);
        int scaledScreenW = (int) (screenW / titleScale);
        int scaledY = (int) (titleY / titleScale);
        for (FormattedCharSequence line : titleLines) {
            int lx = (scaledScreenW - font.width(line)) / 2;
            drawContext.drawString(font, line, lx, scaledY, CommonColors.WHITE, true);
            scaledY += titleLineHeight;
        }
        drawContext.pose().popMatrix();

        if (!userLines.isEmpty()) {
            int userY = titleY + titleBlockHeight + 4;
            int innerScreenW = (int) (screenW / EVENT_USER_NAME_SCALE);
            drawContext.pose().pushMatrix();
            drawContext.pose().translate(0.0F, (float) userY);
            drawContext.pose().scale(EVENT_USER_NAME_SCALE, EVENT_USER_NAME_SCALE);
            int uy = 0;
            for (FormattedCharSequence userLine : userLines) {
                int userX = (innerScreenW - font.width(userLine)) / 2;
                drawContext.drawString(font, userLine, userX, uy, CommonColors.WHITE, true);
                uy += userLineHeight;
            }
            drawContext.pose().popMatrix();
        }
    }

    public void endChaos() {
        eventAnnouncementTitle = null;
        eventAnnouncementUser = null;
        eventAnnouncementTicksLeft = 0;

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
