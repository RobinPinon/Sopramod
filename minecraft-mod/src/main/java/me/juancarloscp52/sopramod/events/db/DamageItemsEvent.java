/*
 * Copyright (c) 2021 juancarloscp52
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
import com.poc.sopramod.SopramodTags.ItemTags;
import com.poc.sopramod.events.AbstractInstantEvent;
import com.poc.sopramod.events.EventType;
import net.minecraft.util.Mth;

public class DamageItemsEvent extends AbstractInstantEvent {
    public static final EventType<DamageItemsEvent> TYPE = EventType.builder(DamageItemsEvent::new).build();

    @Override
    public void init() {
        Sopramod.getInstance().eventHandler.getActivePlayers().forEach(serverPlayerEntity -> {
            serverPlayerEntity.getInventory().forEach(itemStack -> {
                if(itemStack.isDamageableItem() && !itemStack.is(ItemTags.DO_NOT_DAMAGE)){
                    itemStack.hurtAndBreak(Mth.ceil((itemStack.getMaxDamage()-itemStack.getDamageValue())*serverPlayerEntity.getRandom().nextFloat()), serverPlayerEntity.level(), serverPlayerEntity, item -> {});
                }
            });
        });
    }

    @Override
    public EventType<DamageItemsEvent> getType() {
        return TYPE;
    }
}
