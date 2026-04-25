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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.poc.sopramod.client.integrations.discord.DiscordIntegrationSettings;
import com.poc.sopramod.client.integrations.twitch.TwitchIntegrationSettings;
import com.poc.sopramod.client.integrations.youtube.YouTubeIntegrationSettings;

public class SopramodIntegrationsSettings {
    public static final Codec<SopramodIntegrationsSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
        DiscordIntegrationSettings.CODEC.fieldOf("discord").orElseGet(DiscordIntegrationSettings::new).forGetter(s -> s.discord),
        TwitchIntegrationSettings.CODEC.fieldOf("twitch").orElseGet(TwitchIntegrationSettings::new).forGetter(s -> s.twitch),
        YouTubeIntegrationSettings.CODEC.fieldOf("youtube").orElseGet(YouTubeIntegrationSettings::new).forGetter(s -> s.youtube),
        Codec.BOOL.optionalFieldOf("send_chat_messages", true).forGetter(s -> s.sendChatMessages),
        Codec.BOOL.optionalFieldOf("show_current_percentage", true).forGetter(s -> s.showCurrentPercentage),
        Codec.BOOL.optionalFieldOf("show_upcoming_events", true).forGetter(s -> s.showUpcomingEvents)
    ).apply(i, SopramodIntegrationsSettings::new));

    public SopramodIntegrationsSettings(final DiscordIntegrationSettings discord, final TwitchIntegrationSettings twitch, final YouTubeIntegrationSettings youtube, final boolean sendChatMessages, final boolean showCurrentPercentage, final boolean showUpcomingEvents) {
        this.discord = discord;
        this.twitch = twitch;
        this.youtube = youtube;
        this.sendChatMessages = sendChatMessages;
        this.showCurrentPercentage = showCurrentPercentage;
        this.showUpcomingEvents = showUpcomingEvents;
    }

    public SopramodIntegrationsSettings() {
    }

    public DiscordIntegrationSettings discord = new DiscordIntegrationSettings();
    public TwitchIntegrationSettings twitch = new TwitchIntegrationSettings();
    public YouTubeIntegrationSettings youtube = new YouTubeIntegrationSettings();

    public boolean sendChatMessages = true;
    public boolean showCurrentPercentage = true;
    public boolean showUpcomingEvents = true;

    public boolean shouldUseAlternateOffsets() {
        return twitch.enabled || youtube.enabled;
    }
}
