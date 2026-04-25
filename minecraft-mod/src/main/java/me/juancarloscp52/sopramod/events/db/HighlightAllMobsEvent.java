package com.poc.sopramod.events.db;

import com.poc.sopramod.Sopramod;
import com.poc.sopramod.SopramodTags.EntityTypeTags;
import com.poc.sopramod.events.AbstractTimedEvent;
import com.poc.sopramod.events.EventType;
import com.poc.sopramod.server.ServerEventHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.List;

public class HighlightAllMobsEvent extends AbstractTimedEvent {
    public static final EventType<HighlightAllMobsEvent> TYPE = EventType.builder(HighlightAllMobsEvent::new).build();

    @Override
    public void tick() {
        ServerEventHandler eventHandler = Sopramod.getInstance().eventHandler;
        List<ServerLevel> worlds = new ArrayList<>();
        for(var player : eventHandler.getActivePlayers()) {
            ServerLevel playerWorld = player.level();
            if(!worlds.contains(playerWorld))
                worlds.add(playerWorld);
        }
        for(var world : worlds)
            for(var entity : world.getAllEntities())
                if(entity instanceof Mob && !entity.getType().is(EntityTypeTags.DO_NOT_HIGHLIGHT))
                    ((Mob)entity).addEffect(new MobEffectInstance(MobEffects.GLOWING, 2));
        super.tick();
    }

    @Override
    public EventType<HighlightAllMobsEvent> getType() {
        return TYPE;
    }
}
