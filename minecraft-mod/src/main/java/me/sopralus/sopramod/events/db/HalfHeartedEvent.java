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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.HashMap;
import java.util.Map;

public class HalfHeartedEvent extends AbstractTimedEvent {
    public static final EventType<HalfHeartedEvent> TYPE = EventType.builder(HalfHeartedEvent::new).category(EventCategory.HEALTH).build();
    private Map<ServerPlayer, Health> previousHealth = new HashMap<>();

    /** Vie max effective au premier tick (inclut cœurs en plus type AddHeart), pas seulement le base value de l’attribut. */
    private record Health(float currentHealth, float maxHealthEffective) {}

    @Override
    public void init() {
        Sopramod.getInstance().eventHandler.getActivePlayers().forEach(this::adjustPlayerHealth);
    }

    private void adjustPlayerHealth(ServerPlayer serverPlayerEntity) {
        AttributeInstance maxHealthAttribute = serverPlayerEntity.getAttribute(Attributes.MAX_HEALTH);
        previousHealth.computeIfAbsent(serverPlayerEntity, player ->
            new Health(player.getHealth(), player.getMaxHealth()));
        maxHealthAttribute.setBaseValue(1.0);

        if (serverPlayerEntity.getHealth() > 1.0F) {
            serverPlayerEntity.setHealth(1.0F);
        }
    }

    @Override
    public void end() {
        Sopramod.getInstance().eventHandler.getActivePlayers().forEach(this::endPlayer);
        super.end();
    }

    @Override
    public void endPlayer(ServerPlayer player) {
        Health health = previousHealth.get(player);
        if (health != null) {
            float cap = health.maxHealthEffective();
            player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(cap);
            player.setHealth(Math.min(health.currentHealth(), cap));
        }
    }

    @Override
    public void tick() {
        if (this.getTickCount() % 20 == 0) {
            Sopramod.getInstance().eventHandler.getActivePlayers().forEach(this::adjustPlayerHealth);
        }
        super.tick();
    }

    @Override
    public short getDuration() {
        return (short) (super.getDuration() * 1.25);
    }

    @Override
    public EventType<HalfHeartedEvent> getType() {
        return TYPE;
    }
}
