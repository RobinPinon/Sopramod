/**
 * @author Kanawanagasaki
 */

package com.poc.sopramod.events.db;

import com.poc.sopramod.Sopramod;
import com.poc.sopramod.SopramodTags.EntityTypeTags;
import com.poc.sopramod.events.AbstractTimedEvent;
import com.poc.sopramod.events.EventCategory;
import com.poc.sopramod.events.EventType;
import com.poc.sopramod.server.ServerEventHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class InvisibleEveryoneEvent extends AbstractTimedEvent {
    public static final EventType<InvisibleEveryoneEvent> TYPE = EventType.builder(InvisibleEveryoneEvent::new).category(EventCategory.INVISIBILITY).build();

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
                if(shouldBeInvisible(entity))
                    ((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 2));
        super.tick();
    }

    public boolean shouldBeInvisible(Entity entity) {
        return entity instanceof LivingEntity && !entity.getType().is(EntityTypeTags.NOT_INVISIBLE);
    }

    @Override
    public EventType<? extends InvisibleEveryoneEvent> getType() {
        return TYPE;
    }
}
