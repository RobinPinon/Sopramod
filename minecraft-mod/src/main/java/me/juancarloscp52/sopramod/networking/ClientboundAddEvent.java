package com.poc.sopramod.networking;

import com.poc.sopramod.events.Event;
import com.poc.sopramod.events.EventRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundAddEvent(Event event, String triggeredBy) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundAddEvent> CODEC = StreamCodec.composite(
        EventRegistry.STREAM_CODEC, ClientboundAddEvent::event,
        ByteBufCodecs.STRING_UTF8, ClientboundAddEvent::triggeredBy,
        ClientboundAddEvent::new
    );

    @Override
    public Type<ClientboundAddEvent> type() {
        return NetworkingConstants.ADD_EVENT;
    }
}
