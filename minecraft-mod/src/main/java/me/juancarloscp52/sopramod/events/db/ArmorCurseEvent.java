/**
 * @author Curio-sitas
 */

package com.poc.sopramod.events.db;

import com.poc.sopramod.Sopramod;
import com.poc.sopramod.SopramodUtils;
import com.poc.sopramod.events.AbstractInstantEvent;
import com.poc.sopramod.events.EventType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantments;

public class ArmorCurseEvent extends AbstractInstantEvent {
    public static final EventType<ArmorCurseEvent> TYPE = EventType.builder(ArmorCurseEvent::new).build();

    @Override
    public void init() {
        Sopramod.getInstance().eventHandler.getActivePlayers().forEach(
                serverPlayerEntity ->
                    SopramodUtils.modifyArmor(serverPlayerEntity, item ->
                        item.enchant(serverPlayerEntity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.BINDING_CURSE).get(), 1)
                    )
        );
    }

    @Override
    public EventType<ArmorCurseEvent> getType() {
        return TYPE;
    }
}
