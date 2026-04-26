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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.wolf.Wolf;

public class AngryDogsEvent extends AbstractInstantEvent {

    private static final int DOG_COUNT = 5;

    public static final EventType<AngryDogsEvent> TYPE = EventType.builder(AngryDogsEvent::new).build();

    @Override
    public void init() {
        Sopramod.getInstance().eventHandler.getActivePlayers().forEach(this::spawnAngryDogs);
    }

    private void spawnAngryDogs(ServerPlayer player) {
        ServerLevel level = player.level();
        for (int i = 0; i < DOG_COUNT; i++) {
            double angle = (Math.PI * 2.0 * i) / DOG_COUNT;
            double dx = Math.cos(angle) * 2.5;
            double dz = Math.sin(angle) * 2.5;
            double x = player.getX() + dx;
            double z = player.getZ() + dz;
            double y = player.getY();

            Wolf wolf = new Wolf(EntityType.WOLF, level);
            wolf.setPos(x, y, z);
            wolf.setYRot((float) (Math.toDegrees(Math.atan2(-(x - player.getX()), z - player.getZ()))));
            wolf.setXRot(0.0F);
            wolf.setCustomName(Component.literal("chien tueur"));
            wolf.setCustomNameVisible(true);
            wolf.setPersistentAngerTarget(EntityReference.of(player));
            wolf.startPersistentAngerTimer();
            wolf.setTarget(player);

            level.addFreshEntity(wolf);
        }
    }

    @Override
    public EventType<AngryDogsEvent> getType() {
        return TYPE;
    }
}
