package com.poc.sopramod.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * C2S: rachat de point de chaîne correspondant à une récompense nommée (résolue côté client via Helix),
 * par ex. {@code on_recommence} pour le titre &quot;ON RECOMMENCE !!&quot; sur Twitch.
 */
public record ServerboundTwitchEventRedeem(String redeemerLogin, String eventId) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, ServerboundTwitchEventRedeem> CODEC = StreamCodec.composite(
        ByteBufCodecs.stringUtf8(128), ServerboundTwitchEventRedeem::redeemerLogin,
        ByteBufCodecs.stringUtf8(256), ServerboundTwitchEventRedeem::eventId,
        ServerboundTwitchEventRedeem::new
    );

    @Override
    public Type<ServerboundTwitchEventRedeem> type() {
        return NetworkingConstants.TWITCH_NAMED_EVENT_REDEEM;
    }
}
