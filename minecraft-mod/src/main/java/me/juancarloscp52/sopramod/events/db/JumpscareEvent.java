package com.poc.sopramod.events.db;

import com.poc.sopramod.Sopramod;
import com.poc.sopramod.events.AbstractInstantEvent;
import com.poc.sopramod.events.EventType;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;

public class JumpscareEvent extends AbstractInstantEvent {
    public static final EventType<JumpscareEvent> TYPE = EventType.builder(JumpscareEvent::new).build();

    @Override
    public void init() {
        Sopramod.getInstance().eventHandler.getActivePlayers().forEach(player -> player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT, 1.0f)));
    }

    @Override
    public EventType<JumpscareEvent> getType() {
        return TYPE;
    }
}
