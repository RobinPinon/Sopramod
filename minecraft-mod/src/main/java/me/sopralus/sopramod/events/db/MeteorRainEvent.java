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
import com.poc.sopramod.events.AbstractTimedEvent;
import com.poc.sopramod.events.EventCategory;
import com.poc.sopramod.events.EventType;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class MeteorRainEvent extends AbstractTimedEvent {

    public static final EventType<MeteorRainEvent> TYPE = EventType.builder(MeteorRainEvent::new).category(EventCategory.RAIN).build();
    Random random;

    @Override
    public void init() {
        random = new Random();
    }

    @Override
    public void tick() {

        if (getTickCount() % 20 == 0) {
            for (int i = 0; i < 7; i++) {
                Sopramod.getInstance().eventHandler.getActivePlayers().forEach(serverPlayerEntity -> {
                    LargeFireball meteor = new LargeFireball(serverPlayerEntity.level(), serverPlayerEntity, new Vec3(0, -1 * (random.nextInt(4) + 1), 0), 2);
                    meteor.snapTo(serverPlayerEntity.getX() + (random.nextInt(100) - 50), serverPlayerEntity.getY() + 50 + (random.nextInt(10) - 5), serverPlayerEntity.getZ() + (random.nextInt(100) - 50), meteor.getYRot(), meteor.getXRot());
                    serverPlayerEntity.level().addFreshEntity(meteor);
                });

            }
        }
        if (getTickCount() == getTickCount() / 2) {
            Sopramod.getInstance().eventHandler.getActivePlayers().forEach(serverPlayerEntity -> {
                LargeFireball meteor = new LargeFireball(serverPlayerEntity.level(), serverPlayerEntity, new Vec3(0, -1 * (random.nextInt(4) + 1), 0), 4);
                meteor.snapTo(serverPlayerEntity.getX() + (random.nextInt(100) - 50), serverPlayerEntity.getY() + 50 + (random.nextInt(10) - 5), serverPlayerEntity.getZ() + (random.nextInt(100) - 50), meteor.getYRot(), meteor.getXRot());
                serverPlayerEntity.level().addFreshEntity(meteor);
            });
        }
        super.tick();
    }

    @Override
    public EventType<MeteorRainEvent> getType() {
        return TYPE;
    }
}
