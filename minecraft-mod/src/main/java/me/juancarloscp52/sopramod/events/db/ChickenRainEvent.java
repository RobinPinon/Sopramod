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
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;

import java.util.Random;

public class ChickenRainEvent extends AbstractTimedEvent {

    public static final EventType<ChickenRainEvent> TYPE = EventType.builder(ChickenRainEvent::new).category(EventCategory.RAIN).build();
    Random random;

    @Override
    public void init() {
        random = new Random();
    }

    @Override
    public void tick() {

        if (getTickCount() % 20 == 0) {
            for (int i = 0; i < 5; i++) {
                Sopramod.getInstance().eventHandler.getActivePlayers().forEach(serverPlayerEntity ->
                        EntityType.CHICKEN.spawn(serverPlayerEntity.level(), serverPlayerEntity.blockPosition().offset((random.nextInt(100) - 50), 50, (random.nextInt(100) - 50)), EntitySpawnReason.EVENT));
            }
        }
        super.tick();
    }

    @Override
    public EventType<ChickenRainEvent> getType() {
        return TYPE;
    }
}
