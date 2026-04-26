package com.poc.sopramod.events.db;

import com.poc.sopramod.Sopramod;
import com.poc.sopramod.events.AbstractInstantEvent;
import com.poc.sopramod.events.EventCategory;
import com.poc.sopramod.events.EventType;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class RemoveHeartEvent extends AbstractInstantEvent {
    public static final EventType<RemoveHeartEvent> TYPE = EventType.builder(RemoveHeartEvent::new).category(EventCategory.HEALTH).build();

    @Override
    public void init() {
        for (var player : Sopramod.getInstance().eventHandler.getActivePlayers()) {
            if (player.getMaxHealth() > 2) {
                player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(player.getMaxHealth() - 2);
                if(player.getHealth() > player.getMaxHealth()) // Set players health to max allowed health if it was previously bigger than the new Max health.
                    player.setHealth(player.getMaxHealth());
            }
        }
    }

    @Override
    public EventType<RemoveHeartEvent> getType() {
        return TYPE;
    }
}
