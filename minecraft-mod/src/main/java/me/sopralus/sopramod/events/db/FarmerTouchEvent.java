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
import com.poc.sopramod.SopramodTags;
import com.poc.sopramod.events.AbstractTimedEvent;
import com.poc.sopramod.events.EventType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

public class FarmerTouchEvent extends AbstractTimedEvent {

    public static final EventType<FarmerTouchEvent> TYPE = EventType.builder(FarmerTouchEvent::new).build();

    @Override
    public void init() {
        for (var player : Sopramod.getInstance().eventHandler.getActivePlayers()) {
            transformInventory(player);
        }
    }

    @Override
    public void tick() {
        for (var player : Sopramod.getInstance().eventHandler.getActivePlayers()) {
            transformInventory(player);

            int minX = (int) (player.getX() - (player.getX() < 0 ? 1.5 : .5));
            int minY = (int) player.getY() - 1;
            int minZ = (int) (player.getZ() - (player.getZ() < 0 ? 1.5 : .5));
            int maxX = minX + 1;
            int maxY = minY + 3;
            int maxZ = minZ + 1;

            Level world = player.level();

            for (int ix = minX; ix <= maxX; ix++) {
                for (int iy = minY; iy <= maxY; iy++) {
                    for (int iz = minZ; iz <= maxZ; iz++) {
                        BlockPos blockPos = new BlockPos(ix, iy, iz);
                        if (world.getBlockState(blockPos).is(SopramodTags.BlockTags.IGNORED_BY_MIDAS_TOUCH)) {
                            continue;
                        }

                        int odds = player.getRandom().nextInt(100);
                        BlockState below = world.getBlockState(blockPos.below());
                        boolean canGrowWheat = below.is(BlockTags.DIRT) || below.is(Blocks.FARMLAND);
                        if (odds < 22 && canGrowWheat) {
                            BlockState wheat = Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7);
                            world.setBlockAndUpdate(blockPos, wheat);
                        } else {
                            world.setBlockAndUpdate(blockPos, Blocks.HAY_BLOCK.defaultBlockState());
                        }
                    }
                }
            }
        }

        super.tick();
    }

    private static void transformInventory(ServerPlayer player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            inv.setItem(i, transformStack(stack));
        }
    }

    private static ItemStack transformStack(ItemStack itemStack) {
        if (itemStack.isEmpty() || itemStack.is(SopramodTags.ItemTags.IGNORED_BY_MIDAS_TOUCH)) {
            return itemStack;
        }

        Item item = itemStack.getItem();
        int count = itemStack.getCount();

        if (itemStack.has(DataComponents.FOOD)) {
            return new ItemStack(Items.POTATO, Math.min(count, 64));
        }
        if (item instanceof BlockItem) {
            return new ItemStack(Items.DIRT, Math.min(count, 64));
        }
        if (itemStack.has(DataComponents.EQUIPPABLE)) {
            return new ItemStack(Items.DIRT, Math.min(count, 64));
        }
        if (item instanceof AxeItem || item instanceof ShovelItem || item instanceof HoeItem) {
            return new ItemStack(Items.IRON_HOE, 1);
        }
        if (itemStack.has(DataComponents.TOOL)) {
            Tool tool = itemStack.get(DataComponents.TOOL);
            List<Tool.Rule> rules = tool.rules();
            for (Tool.Rule rule : rules) {
                Optional<TagKey<Block>> key = rule.blocks().unwrapKey();
                if (key.isEmpty()) {
                    continue;
                }
                TagKey<Block> tag = key.get();
                if (tag.equals(BlockTags.MINEABLE_WITH_PICKAXE)
                    || tag.equals(BlockTags.SWORD_EFFICIENT)
                    || tag.equals(BlockTags.MINEABLE_WITH_AXE)
                    || tag.equals(BlockTags.MINEABLE_WITH_SHOVEL)) {
                    return new ItemStack(Items.IRON_HOE, 1);
                }
            }
        }

        return new ItemStack(Items.DIRT, Math.min(count, 64));
    }

    @Override
    public short getDuration() {
        return (short) (super.getDuration() * .2);
    }

    @Override
    public EventType<FarmerTouchEvent> getType() {
        return TYPE;
    }
}
