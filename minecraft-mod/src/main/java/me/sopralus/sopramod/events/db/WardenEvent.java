/**
 * @author Curiositas
 */

package com.poc.sopramod.events.db;

import com.poc.sopramod.Sopramod;
import com.poc.sopramod.events.AbstractInstantEvent;
import com.poc.sopramod.events.EventType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.warden.Warden;


public class WardenEvent extends AbstractInstantEvent {
    public static final EventType<WardenEvent> TYPE = EventType.builder(WardenEvent::new).build();

    @Override
    public void init() {
        Sopramod.getInstance().eventHandler.getActivePlayers().forEach(
                serverPlayerEntity -> {
                    Warden warden = EntityType.WARDEN.spawn(serverPlayerEntity.level(), serverPlayerEntity.blockPosition(), EntitySpawnReason.EVENT);
                    if (warden != null) {
                        warden.setHealth(warden.getMaxHealth());
                    }
                }
        );
    }

    @Override
    public EventType<WardenEvent> getType() {
        return TYPE;
    }
}
