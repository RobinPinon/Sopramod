package com.poc.sopramod.events.db;

import com.poc.sopramod.Sopramod;
import com.poc.sopramod.SopramodTags.EnchantmentTags;
import com.poc.sopramod.events.AbstractInstantEvent;
import com.poc.sopramod.events.EventType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class RemoveEnchantmentsEvent extends AbstractInstantEvent {
   public static final EventType<RemoveEnchantmentsEvent> TYPE = EventType.builder(RemoveEnchantmentsEvent::new).build();

    @Override
    public void init() {

        Sopramod.getInstance().eventHandler.getActivePlayers().forEach(player -> {

            player.getInventory().forEach(itemStack -> {
                removeEnchant(itemStack);
            });
        });
    }

    private void removeEnchant(ItemStack itemStack) {
        if (Math.random() > 1.0/3) {
            // one chance over 3 to remove an enchantment.
            return;
        }

        EnchantmentHelper.updateEnchantments(itemStack, enchantments ->
            enchantments.removeIf(enchantment -> !enchantment.is(EnchantmentTags.DO_NOT_REMOVE))
        );
    }

    @Override
    public EventType<RemoveEnchantmentsEvent> getType() {
        return TYPE;
    }
}
