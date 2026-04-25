package com.poc.sopramod.client.integrations;

import com.poc.sopramod.client.ClientEventHandler;
import com.poc.sopramod.client.SopramodIntegrationsSettings;
import com.poc.sopramod.client.VotingClient;
import com.poc.sopramod.client.integrations.discord.DiscordIntegration;
import com.poc.sopramod.client.integrations.twitch.TwitchIntegration;
import com.poc.sopramod.client.integrations.youtube.YoutubeIntegration;

import java.util.function.BiFunction;
import java.util.function.Function;

public enum IntegrationType {
    TWITCH((handler, client) -> new TwitchIntegration(client), s -> s.twitch),
    DISCORD((handler, client) -> new DiscordIntegration(client), s -> s.discord),
    YOUTUBE(YoutubeIntegration::new, s -> s.youtube),
    ;

    private final BiFunction<ClientEventHandler, VotingClient, Integration> constructor;
    private final Function<SopramodIntegrationsSettings, IntegrationSettings> settingsGetter;

    IntegrationType(final BiFunction<ClientEventHandler, VotingClient, Integration> constructor, final Function<SopramodIntegrationsSettings, IntegrationSettings> settingsGetter) {
        this.constructor = constructor;
        this.settingsGetter = settingsGetter;
    }

    public Integration create(ClientEventHandler handler, VotingClient client) {
        return constructor.apply(handler, client);
    }

    public IntegrationSettings settings(final SopramodIntegrationsSettings settings) {
        return settingsGetter.apply(settings);
    }
}
