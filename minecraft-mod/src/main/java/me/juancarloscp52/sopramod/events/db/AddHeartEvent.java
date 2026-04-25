package com.poc.sopramod.events.db;

import com.poc.sopramod.Sopramod;
import com.poc.sopramod.events.AbstractInstantEvent;
import com.poc.sopramod.events.EventCategory;
import com.poc.sopramod.events.EventType;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class AddHeartEvent extends AbstractInstantEvent {
    public static final EventType<AddHeartEvent> TYPE = EventType.builder(AddHeartEvent::new).category(EventCategory.HEALTH).build();

    @Override
    public void init() {
        for(var player : Sopramod.getInstance().eventHandler.getActivePlayers()) {
            player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(player.getMaxHealth() + 2);
            player.setHealth(player.getHealth() + 2);
        }
    }

    @Override
    public EventType<AddHeartEvent> getType() {
        return TYPE;
    }
}
