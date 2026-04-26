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
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class ExcaliburEvent extends AbstractInstantEvent {

    public static final EventType<ExcaliburEvent> TYPE = EventType.builder(ExcaliburEvent::new).build();

    /** Poids : bois fréquent, netherite rare ; cuivre entre pierre et or. */
    private static final Item[] SWORD_ITEMS = {
        Items.WOODEN_SWORD,
        Items.STONE_SWORD,
        Items.COPPER_SWORD,
        Items.GOLDEN_SWORD,
        Items.IRON_SWORD,
        Items.DIAMOND_SWORD,
        Items.NETHERITE_SWORD
    };

    private static final int[] SWORD_WEIGHTS = { 40, 20, 15, 11, 8, 5, 1 };

    @Override
    public void init() {
        Sopramod.getInstance().eventHandler.getActivePlayers().forEach(this::giveExcalibur);
    }

    private void giveExcalibur(ServerPlayer player) {
        RandomSource random = player.getRandom();
        Item swordItem = rollSwordTier(random);
        ItemStack sword = new ItemStack(swordItem);

        Registry<Enchantment> enchantments = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        int sharpness = random.nextBoolean() ? 3 : 5;
        sword.enchant(holder(enchantments, Enchantments.SHARPNESS), sharpness);
        sword.enchant(holder(enchantments, Enchantments.FLAME), 1);
        sword.enchant(holder(enchantments, Enchantments.UNBREAKING), 3);

        sword.set(DataComponents.CUSTOM_NAME, Component.translatable("events.sopramod.excalibur.item_name"));

        ServerLevel level = player.level();
        if (!player.getInventory().add(sword)) {
            player.spawnAtLocation(level, sword);
        }
    }

    private static Holder<Enchantment> holder(Registry<Enchantment> registry, ResourceKey<Enchantment> key) {
        return registry.get(key).orElseThrow();
    }

    private static Item rollSwordTier(RandomSource random) {
        int total = 0;
        for (int w : SWORD_WEIGHTS) {
            total += w;
        }
        int roll = random.nextInt(total);
        int acc = 0;
        for (int i = 0; i < SWORD_WEIGHTS.length; i++) {
            acc += SWORD_WEIGHTS[i];
            if (roll < acc) {
                return SWORD_ITEMS[i];
            }
        }
        return SWORD_ITEMS[SWORD_ITEMS.length - 1];
    }

    @Override
    public EventType<ExcaliburEvent> getType() {
        return TYPE;
    }
}
