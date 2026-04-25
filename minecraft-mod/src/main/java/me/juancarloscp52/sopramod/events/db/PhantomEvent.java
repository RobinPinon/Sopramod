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

package com.poc.sopramod.events.db;

import com.poc.sopramod.Sopramod;
import com.poc.sopramod.events.AbstractInstantEvent;
import com.poc.sopramod.events.EventType;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Phantom;

public class PhantomEvent extends AbstractInstantEvent {
    public static final EventType<PhantomEvent> TYPE = EventType.builder(PhantomEvent::new).build();

    @Override
    public void init() {
        Sopramod.getInstance().eventHandler.getActivePlayers().forEach(serverPlayerEntity -> {
            for(int i =0; i<3;i++){
                Phantom phantom = EntityType.PHANTOM.spawn(serverPlayerEntity.level(), serverPlayerEntity.blockPosition().relative(Direction.UP,5), EntitySpawnReason.EVENT);
                if(null!=phantom)
                    phantom.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,460));
            }
        });
    }

    @Override
    public EventType<PhantomEvent> getType() {
        return TYPE;
    }
}
