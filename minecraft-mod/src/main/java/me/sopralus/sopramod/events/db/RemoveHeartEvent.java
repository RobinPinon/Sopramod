package com.poc.sopramod.events.db;

import com.poc.sopramod.Sopramod;
import com.poc.sopramod.events.AbstractInstantEvent;
import com.poc.sopramod.events.EventCategory;
import com.poc.sopramod.events.EventType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class RemoveHeartEvent extends AbstractInstantEvent {
    public static final EventType<RemoveHeartEvent> TYPE = EventType.builder(RemoveHeartEvent::new).category(EventCategory.HEALTH).build();

    @Override
    public void init() {
        for (var player : Sopramod.getInstance().eventHandler.getActivePlayers()) {
            AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealthAttr == null) {
                continue;
            }
            if (maxHealthAttr.getBaseValue() > 2.0) {
                Sopramod.getInstance().playerHeartStorage.setAndSave(player, maxHealthAttr.getBaseValue() - 2.0);
            }
        }
    }

    @Override
    public EventType<RemoveHeartEvent> getType() {
        return TYPE;
    }
}
