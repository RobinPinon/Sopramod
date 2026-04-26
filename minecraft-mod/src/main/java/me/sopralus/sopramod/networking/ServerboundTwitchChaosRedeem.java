package com.poc.sopramod.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * C2S: le client ayant reçu un rachat de points de chaîne Twitch correspondant à
 * l’événement chaos envoie le login du spectateur (affichage HUD).
 */
public record ServerboundTwitchChaosRedeem(String redeemerLogin) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, ServerboundTwitchChaosRedeem> CODEC = StreamCodec.composite(
        ByteBufCodecs.stringUtf8(128), ServerboundTwitchChaosRedeem::redeemerLogin,
        ServerboundTwitchChaosRedeem::new
    );

    @Override
    public Type<ServerboundTwitchChaosRedeem> type() {
        return NetworkingConstants.TWITCH_CHAOS_REDEEM;
    }
}
