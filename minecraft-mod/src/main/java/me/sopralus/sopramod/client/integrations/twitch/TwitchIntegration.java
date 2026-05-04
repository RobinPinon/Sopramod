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

package com.poc.sopramod.client.integrations.twitch;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.poc.sopramod.client.SopramodClient;
import com.poc.sopramod.client.SopramodIntegrationsSettings;
import com.poc.sopramod.client.VotingClient;
import com.poc.sopramod.client.integrations.Integration;
import com.poc.sopramod.networking.ServerboundTwitchChaosRedeem;
import com.poc.sopramod.networking.ServerboundTwitchEventRedeem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.cap.EnableCapHandler;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectAttemptFailedEvent;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.ExceptionEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PingEvent;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Résolution du nom de la récompense: tag IRC {@code custom-reward-id} + API Helix
 * (même token OAuth que le chat), sans paramètre supplémentaire. Le token doit inclure
 * {@code channel:read:redemptions} (ou {@code channel:manage:redemptions}).
 * Cooldown 5 minutes par login et par type de récompense : le mod n’envoie pas l’événement Minecraft
 * (les points Twitch restent côté file / à traiter manuellement sur le dashboard).
 * Récompense reconnue: un event aléatoire côté serveur
 * (même logique de pool que le timer / vote) lorsque le titre Helix est exactement
 * "DU CHAOS !!!!!!" (récompense côté Twitch / tableau des récompenses).
 * Idem, récompense dont le titre est exactement {@link #ON_RECOMMENCE_REWARD_TITLE} : event forcé
 * {@code on_recommence} (même règles de titre que la clé d’événement / le dashboard Twitch).
 */
public class TwitchIntegration extends ListenerAdapter implements Integration {

    /** Nom de la récompense côté Twitch (développeur / table des récompenses). */
    private static final String CHAOS_REWARD_TITLE = "DU CHAOS !!!!!!";
    /** Titre sur Twitch = libellé event « ON RECOMMENCE !! » (doit coller caractère pour caractère au tableau des récompenses côté chaîne, après trim côté Helix). */
    private static final String ON_RECOMMENCE_REWARD_TITLE = "ON RECOMMENCE !!";
    private static final String ON_RECOMMENCE_EVENT_ID = "on_recommence";
    private static final long REWARDS_CACHE_TTL_MS = 5 * 60_000L;
    /** Délai minimum entre deux rachats du même spectateur pour une même récompense (chaos / on recommence), en ms. */
    private static final long TWITCH_REDEMPTION_COOLDOWN_MS = Duration.ofMinutes(5).toMillis();

    private static final HttpClient HTTP = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    private static boolean loggedMissingRedemptionScope;

    private final ConcurrentHashMap<String, Long> chaosCooldownStartMsByLogin = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> recommenceCooldownStartMsByLogin = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> redeemLocksByLoginLower = new ConcurrentHashMap<>();

    private Configuration config;
    private final SopramodIntegrationsSettings settings = SopramodClient.getInstance().integrationsSettings;
    private PircBotX ircChatBot;
    private ExecutorService botExecutor;
    private final VotingClient votingClient;
    private long lastJoinMessage = 0;

    private String cachedClientId;
    private String cachedBroadcasterId;
    private final Map<String, String> rewardIdToTitle = new ConcurrentHashMap<>();
    private long rewardsCacheExpiresAt;

    public TwitchIntegration(VotingClient votingClient) {
        this.votingClient = votingClient;
        String ircLogin = SopramodClient.resolveTwitchIrcLogin();
        if (ircLogin == null || ircLogin.isEmpty()) {
            ircLogin = settings.twitch.channel == null ? "" : settings.twitch.channel.toLowerCase();
        }
        String channel = settings.twitch.channel == null ? "" : settings.twitch.channel.toLowerCase().trim();
        String pass = settings.twitch.token == null ? "" : settings.twitch.token.trim();
        if (pass.isEmpty()) {
            SopramodClient.LOGGER.error("[Twitch] token vide : impossible de se connecter à l’IRC. Remplis le token OAuth (scopes chat:read + chat:write).");
            return;
        }
        if (channel.isEmpty()) {
            SopramodClient.LOGGER.error("[Twitch] nom de chaîne vide : définis le channel (login sans #).");
            return;
        }
        if (ircLogin.isEmpty()) {
            SopramodClient.LOGGER.error("[Twitch] NICK IRC vide : il doit être le login du compte du token (souvent identique au channel si tu joues sur ta propre chaîne).");
            return;
        }
        SopramodClient.LOGGER.info("[Twitch] Connexion IRC — NICK={} (compte du token), salon=#{}", ircLogin, channel);
        config = new Configuration.Builder()
            .setAutoNickChange(false)
            .setOnJoinWhoEnabled(false)
            .setCapEnabled(true)
            .addCapHandler(new EnableCapHandler("twitch.tv/tags"))
            .addCapHandler(new EnableCapHandler("twitch.tv/commands"))
            .addCapHandler(new EnableCapHandler("twitch.tv/membership"))
            .setEncoding(StandardCharsets.UTF_8)
            .addServer("irc.chat.twitch.tv", 6697)
            .setSocketFactory(SSLSocketFactory.getDefault())
            .setName(ircLogin)
            .setServerPassword(pass.startsWith("oauth:") ? pass : "oauth:" + pass)
            .addAutoJoinChannel("#" + channel)
            .addListener(this)
            .setAutoSplitMessage(false)
            .buildConfiguration();
        this.start();
    }

    @Override
    public void start() {
        if (config == null) {
            return;
        }
        ircChatBot = new PircBotX(config);
        botExecutor = Executors.newCachedThreadPool();
        botExecutor.execute(() -> {
            try {
                ircChatBot.startBot();
            } catch (IOException e) {
                SopramodClient.LOGGER.error("Erreur d’E/S au démarrage du bot IRC : {}", e.getMessage());
                e.printStackTrace();
            } catch (IrcException e) {
                SopramodClient.LOGGER.error("Erreur IRC au démarrage du bot : {}", e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void stop() {
        if (ircChatBot != null) {
            ircChatBot.stopBotReconnect();
            ircChatBot.close();
        }
        if (botExecutor != null) {
            botExecutor.shutdown();
        }
    }

    @Override
    public void onConnect(ConnectEvent event) {
        SopramodClient.LOGGER.info("[Twitch] IRC connecté (tmi), salon #{} visible dans un instant.", settings.twitch.channel == null ? "?" : settings.twitch.channel.toLowerCase());
    }

    @Override
    public void onConnectAttemptFailed(ConnectAttemptFailedEvent event) {
        SopramodClient.LOGGER.error("[Twitch] Échec de connexion IRC : {}", event.getConnectExceptions());
    }

    @Override
    public void onException(ExceptionEvent event) {
        SopramodClient.LOGGER.error("[Twitch] Erreur IRC: {} — {}", event.getMessage(), event.getException() != null ? event.getException().getMessage() : "");
    }

    @Override
    public void onMessage(MessageEvent event) {
        SopramodClient.getInstance().clientEventHandler.votingClient.processVote(event.getMessage(), event.getUser().getLogin());
        considerChannelPointCustomRewards(event);
    }

    @Override
    public void onJoin(JoinEvent event) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastJoinMessage > 30000) {
            votingClient.sendMessage(I18n.get("sopramod.chat.twitch_connected"));
            lastJoinMessage = currentTime;
        }
    }

    @Override
    public void onPing(PingEvent event) {
        if (ircChatBot == null) {
            return;
        }
        ircChatBot.sendRaw().rawLineNow(String.format("PONG %s\r\n", event.getPingValue()));
    }

    @Override
    public void sendPoll(int voteID, List<Component> events) {
        if (ircChatBot == null) {
            return;
        }
        int altOffset = voteID % 2 == 0 ? 4 : 0;
        StringBuilder stringBuilder = new StringBuilder(I18n.get("sopramod.chat.current_poll"));
        for (int i = 0; i < events.size(); i++)
            stringBuilder.append(String.format("[ %d - %s ] ", 1 + i + altOffset, events.get(i).getString()));
        ircChatBot.sendIRC().message("#" + settings.twitch.channel.toLowerCase(), "/me [Sopramod Bot] " + stringBuilder);
    }

    @Override
    public void sendMessage(String message) {
        if (ircChatBot == null) {
            return;
        }
        ircChatBot.sendIRC().message("#" + settings.twitch.channel.toLowerCase(), "/me [Sopramod Bot] " + message);
    }

    @Override
    public int getColor(int alpha) {
        return ARGB.color(alpha, 145, 70, 255);
    }

    private Object redeemLock(String loginLower) {
        Objects.requireNonNull(loginLower);
        return redeemLocksByLoginLower.computeIfAbsent(loginLower, k -> new Object());
    }

    private void considerChannelPointCustomRewards(MessageEvent event) {
        ImmutableMap<String, String> tags = event.getV3Tags();
        if (tags == null || !tags.containsKey("custom-reward-id")) {
            return;
        }
        String rewardId = tags.get("custom-reward-id");
        if (rewardId == null || rewardId.isEmpty()) {
            return;
        }
        String userLogin = event.getUser() != null ? event.getUser().getLogin() : "twitch";
        botExecutor.execute(() -> {
            String helixTitle = getResolvedHelixTitleForCustomRewardId(rewardId);
            if (helixTitle == null) {
                return;
            }
            String t = helixTitle.trim();
            final String redeemerLower = userLogin.toLowerCase(Locale.ROOT);
            synchronized (redeemLock(redeemerLower)) {
                if (CHAOS_REWARD_TITLE.equals(t)) {
                    if (tryConsumeCooldown(redeemerLower, true)) {
                        sendOnMainThreadChaosRedeem(redeemerLower);
                    }
                    return;
                }
                if (ON_RECOMMENCE_REWARD_TITLE.equals(t)) {
                    if (tryConsumeCooldown(redeemerLower, false)) {
                        sendOnMainThreadNamedEventRedeem(redeemerLower, ON_RECOMMENCE_EVENT_ID);
                    }
                }
            }
        });
    }

    /**
     * @return {@code true} si la récompense peut être utilisée (cooldown enregistré et envoi Minecraft autorisé) ;
     *         {@code false} si le spectateur est encore en cooldown (message chat uniquement, pas d’appel Helix).
     */
    private boolean tryConsumeCooldown(String redeemerLoginLower, boolean chaosReward) {
        ConcurrentHashMap<String, Long> map = chaosReward ? chaosCooldownStartMsByLogin : recommenceCooldownStartMsByLogin;
        long now = System.currentTimeMillis();
        Long started = map.get(redeemerLoginLower);
        if (started != null && now - started < TWITCH_REDEMPTION_COOLDOWN_MS) {
            long remainingMs = TWITCH_REDEMPTION_COOLDOWN_MS - (now - started);
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                mc.execute(() -> notifyRedemptionCooldownInChat(redeemerLoginLower, chaosReward, remainingMs));
            }
            return false;
        }
        map.put(redeemerLoginLower, now);
        return true;
    }

    private void notifyRedemptionCooldownInChat(String redeemerLoginLower, boolean chaosReward, long remainingMs) {
        if (ircChatBot == null) {
            return;
        }
        String rewardLangKey = chaosReward ? "sopramod.twitch.reward_name.chaos" : "sopramod.twitch.reward_name.recommence";
        String rewardDisplay = I18n.get(rewardLangKey);
        String timeFmt = formatCooldownRemainingHuman(remainingMs);
        sendMessage(I18n.get("sopramod.twitch.reward_cooldown", redeemerLoginLower, rewardDisplay, timeFmt));
    }

    private static String formatCooldownRemainingHuman(long remainingMs) {
        long secs = Math.max(1L, (remainingMs + 999L) / 1000L);
        long minutes = secs / 60;
        long s = secs % 60;
        if (minutes > 0L) {
            return minutes + " min " + s + " s";
        }
        return s + " s";
    }

    private void sendOnMainThreadChaosRedeem(String userLogin) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().getConnection() == null) {
                return;
            }
            ClientPlayNetworking.send(new ServerboundTwitchChaosRedeem(userLogin));
        });
    }

    private void sendOnMainThreadNamedEventRedeem(String userLogin, String eventId) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().getConnection() == null) {
                return;
            }
            ClientPlayNetworking.send(new ServerboundTwitchEventRedeem(userLogin, eventId));
        });
    }

    /**
     * Titre de la custom reward sur Helix, avec la même logique de cache / rechargement que
     * l’ancienne détection {@code DU CHAOS} (nouvelle récompense, cache expiré, etc.).
     */
    private String getResolvedHelixTitleForCustomRewardId(String rewardId) {
        if (rewardIdToTitle.containsKey(rewardId)) {
            if (System.currentTimeMillis() < rewardsCacheExpiresAt) {
                return rewardIdToTitle.get(rewardId);
            }
        } else {
            if (System.currentTimeMillis() < rewardsCacheExpiresAt) {
                rewardIdToTitle.clear();
                rewardsCacheExpiresAt = 0;
            }
        }
        try {
            if (!loadRewardIdToTitle()) {
                return null;
            }
        } catch (Exception e) {
            SopramodClient.LOGGER.warn("Twitch Helix: impossible de lire les récompenses: {}", e.getMessage());
            return null;
        }
        return rewardIdToTitle.get(rewardId);
    }

    private String plainToken() {
        String t = settings.twitch.token;
        if (t == null) {
            return "";
        }
        t = t.trim();
        if (t.startsWith("oauth:")) {
            return t.substring("oauth:".length());
        }
        return t;
    }

    private boolean loadRewardIdToTitle() throws Exception {
        String accessToken = plainToken();
        if (accessToken.isEmpty()) {
            return false;
        }
        if (cachedClientId == null) {
            cachedClientId = fetchClientIdFromValidate(accessToken);
        }
        if (cachedClientId == null) {
            return false;
        }
        if (cachedBroadcasterId == null) {
            String login = settings.twitch.channel;
            if (login == null || login.isEmpty()) {
                return false;
            }
            cachedBroadcasterId = fetchBroadcasterUserId(cachedClientId, accessToken, login.toLowerCase().trim());
        }
        if (cachedBroadcasterId == null) {
            return false;
        }
        if (!canReadChannelPointRewards(accessToken)) {
            if (!loggedMissingRedemptionScope) {
                loggedMissingRedemptionScope = true;
                SopramodClient.LOGGER.warn("Le token OAuth doit inclure le scope 'channel:read:redemptions' (ou 'channel:manage:redemptions') pour associer 'custom-reward-id' aux titres de récompenses (ex. « " + CHAOS_REWARD_TITLE + " » ou « " + ON_RECOMMENCE_REWARD_TITLE + " »).");
            }
            return false;
        }
        String body = getCustomRewardsPage(cachedClientId, accessToken, cachedBroadcasterId, null);
        Map<String, String> batch = new HashMap<>();
        addRewardsFromResponse(body, batch);
        for (int guard = 0; guard < 8; guard++) {
            String next = readCursor(body);
            if (next == null) {
                break;
            }
            body = getCustomRewardsPage(cachedClientId, accessToken, cachedBroadcasterId, next);
            addRewardsFromResponse(body, batch);
        }
        rewardIdToTitle.clear();
        rewardIdToTitle.putAll(batch);
        rewardsCacheExpiresAt = System.currentTimeMillis() + REWARDS_CACHE_TTL_MS;
        return !rewardIdToTitle.isEmpty();
    }

    private void addRewardsFromResponse(String json, Map<String, String> out) {
        if (json == null || json.isEmpty()) {
            return;
        }
        JsonObject root;
        try {
            root = JsonParser.parseString(json).getAsJsonObject();
        } catch (Exception e) {
            return;
        }
        if (!root.has("data") || !root.get("data").isJsonArray()) {
            return;
        }
        JsonArray data = root.getAsJsonArray("data");
        for (JsonElement el : data) {
            if (!el.isJsonObject()) {
                continue;
            }
            JsonObject o = el.getAsJsonObject();
            if (!o.has("id") || !o.has("title")) {
                continue;
            }
            out.put(o.get("id").getAsString(), o.get("title").getAsString().trim());
        }
    }

    private String readCursor(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (!root.has("pagination") || !root.get("pagination").isJsonObject()) {
                return null;
            }
            JsonObject p = root.getAsJsonObject("pagination");
            if (p.has("cursor") && !p.get("cursor").isJsonNull()) {
                return p.get("cursor").getAsString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String getCustomRewardsPage(String clientId, String accessToken, String broadcasterId, String cursor) throws Exception {
        String idEnc = URLEncoder.encode(broadcasterId, StandardCharsets.UTF_8);
        StringBuilder uri = new StringBuilder("https://api.twitch.tv/helix/channel_points/custom_rewards?broadcaster_id=").append(idEnc);
        if (cursor != null) {
            uri.append("&after=").append(URLEncoder.encode(cursor, StandardCharsets.UTF_8));
        }
        HttpRequest req = HttpRequest.newBuilder(URI.create(uri.toString()))
            .header("Client-Id", clientId)
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            throw new IOException("Helix custom_rewards HTTP " + res.statusCode() + ": " + res.body());
        }
        return res.body();
    }

    private String fetchClientIdFromValidate(String accessToken) {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://id.twitch.tv/oauth2/validate"))
                .header("Authorization", "OAuth " + accessToken)
                .GET()
                .build();
            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() < 200 || res.statusCode() >= 300) {
                SopramodClient.LOGGER.warn("[Twitch] Validation du token : échec HTTP {}", res.statusCode());
                return null;
            }
            JsonObject o = JsonParser.parseString(res.body()).getAsJsonObject();
            if (o.has("client_id") && !o.get("client_id").isJsonNull()) {
                return o.get("client_id").getAsString();
            }
        } catch (Exception e) {
            SopramodClient.LOGGER.warn("[Twitch] Validation du token : {}", e.getMessage());
        }
        return null;
    }

    private String fetchBroadcasterUserId(String clientId, String accessToken, String login) {
        try {
            String u = "https://api.twitch.tv/helix/users?login=" + URLEncoder.encode(login, StandardCharsets.UTF_8);
            HttpRequest req = HttpRequest.newBuilder(URI.create(u))
                .header("Client-Id", clientId)
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() < 200 || res.statusCode() >= 300) {
                return null;
            }
            JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
            if (!root.has("data") || !root.get("data").isJsonArray() || root.getAsJsonArray("data").isEmpty()) {
                return null;
            }
            return root.getAsJsonArray("data").get(0).getAsJsonObject().get("id").getAsString();
        } catch (Exception e) {
            SopramodClient.LOGGER.warn("[Twitch] Recherche utilisateur Helix : {}", e.getMessage());
        }
        return null;
    }

    private boolean canReadChannelPointRewards(String accessToken) {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://id.twitch.tv/oauth2/validate"))
                .header("Authorization", "OAuth " + accessToken)
                .GET()
                .build();
            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() < 200 || res.statusCode() >= 300) {
                return false;
            }
            JsonObject o = JsonParser.parseString(res.body()).getAsJsonObject();
            if (!o.has("scopes") || !o.get("scopes").isJsonArray()) {
                return false;
            }
            for (JsonElement s : o.getAsJsonArray("scopes")) {
                if (s == null || s.isJsonNull()) {
                    continue;
                }
                String scope = s.getAsString();
                if ("channel:read:redemptions".equals(scope) || "channel:manage:redemptions".equals(scope)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
