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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class BigChickenEvent extends AbstractInstantEvent {

    private static final double TARGET_HEIGHT_BLOCKS = 2.0;

    public static final EventType<BigChickenEvent> TYPE = EventType.builder(BigChickenEvent::new).build();

    @Override
    public void init() {
        Sopramod.getInstance().eventHandler.getActivePlayers().forEach(player -> {
            ServerLevel level = player.level();
            Vec3 look = player.getLookAngle();
            Vec3 horizontal = new Vec3(look.x, 0.0, look.z);
            if (horizontal.lengthSqr() < 1.0E-4) {
                horizontal = new Vec3(0.0, 0.0, 1.0);
            } else {
                horizontal = horizontal.normalize();
            }
            Vec3 spawnVec = player.position().add(horizontal.scale(3.5)).add(0.0, 0.25, 0.0);
            BlockPos spawnPos = BlockPos.containing(spawnVec);

            Entity spawned = EntityType.CHICKEN.spawn(level, spawnPos, EntitySpawnReason.EVENT);
            if (!(spawned instanceof Mob chicken)) {
                return;
            }

            double baseHeight = chicken.getBbHeight();
            AttributeInstance scaleAttr = chicken.getAttribute(Attributes.SCALE);
            if (scaleAttr != null && baseHeight > 1.0E-3) {
                scaleAttr.setBaseValue(TARGET_HEIGHT_BLOCKS / baseHeight);
            }
            chicken.refreshDimensions();
            chicken.setPos(spawnVec.x, spawnVec.y, spawnVec.z);

            // Ensure the giant chicken drops one full stack when killed.
            chicken.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CHICKEN, 64));
            chicken.setGuaranteedDrop(EquipmentSlot.MAINHAND);
        });
    }

    @Override
    public EventType<BigChickenEvent> getType() {
        return TYPE;
    }
}
