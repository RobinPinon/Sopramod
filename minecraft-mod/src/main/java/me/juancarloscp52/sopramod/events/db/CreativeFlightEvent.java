package com.poc.sopramod.events.db;

import com.poc.sopramod.Sopramod;
import com.poc.sopramod.events.AbstractTimedEvent;
import com.poc.sopramod.events.EventType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class CreativeFlightEvent extends AbstractTimedEvent {
    public static final EventType<CreativeFlightEvent> TYPE = EventType.builder(CreativeFlightEvent::new).build();

    @Override
    @Environment(EnvType.CLIENT)
    public void initClient() {
        Player player = Minecraft.getInstance().player;
        if(null != player) {
            if (!(player.isCreative() || player.isSpectator())) {
                player.getAbilities().mayfly = true;
            }
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void endClient() {
        super.endClient();
        Player player = Minecraft.getInstance().player;
        if(null != player){
            if(!(player.isCreative() || player.isSpectator())){
                player.getAbilities().mayfly = false;
                player.getAbilities().flying=false;
                player.onUpdateAbilities();
            }
        }
    }

    @Override
    public void init() {
        Sopramod.getInstance().eventHandler.getActivePlayers().forEach(player -> {
            if(!(player.isCreative() || player.isSpectator())) {
                player.getAbilities().mayfly = true;
            }
        });
    }

    @Override
    public void end() {
        super.end();
        Sopramod.getInstance().eventHandler.getActivePlayers().forEach(player -> {
            if(!(player.isCreative() || player.isSpectator())){
                player.getAbilities().mayfly = false;
                player.getAbilities().flying=false;
                player.onUpdateAbilities();
            }
        });
    }

    @Override
    public short getDuration() {
        return (short)(super.getDuration()*0.75);
    }

    @Override
    public EventType<CreativeFlightEvent> getType() {
        return TYPE;
    }
}
