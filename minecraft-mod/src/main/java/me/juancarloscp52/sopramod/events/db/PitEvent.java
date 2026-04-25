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
import com.poc.sopramod.SopramodTags.BlockTags;
import com.poc.sopramod.events.AbstractInstantEvent;
import com.poc.sopramod.events.EventType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

public class PitEvent extends AbstractInstantEvent {
    public static final EventType<PitEvent> TYPE = EventType.builder(PitEvent::new).build();

    @Override
    public void init() {
        Sopramod.getInstance().eventHandler.getActivePlayers().forEach(serverPlayerEntity ->
        {
            BlockPos pos = serverPlayerEntity.blockPosition();
            int x = pos.getX(),y = pos.getY(),z = pos.getZ();
            for(int h = y-50; h<=319; h++){
                for(int i= -9; i<=9;i++) {
                    for (int j = -9; j <= 9; j++) {
                        BlockPos currentPos = new BlockPos(x+i,h,z+j);
                        if(Math.abs(i)>2|| Math.abs(j)>2){
                            if(!serverPlayerEntity.level().getBlockState(currentPos).is(BlockTags.NOT_REPLACED_BY_EVENTS)){
                                if(h<(y-45)){
                                    serverPlayerEntity.level().setBlockAndUpdate(currentPos, Blocks.WATER.defaultBlockState());
                                }else{
                                    serverPlayerEntity.level().setBlockAndUpdate(currentPos, Blocks.AIR.defaultBlockState());
                                }
                            }
                        }
                    }
                }
            }
            serverPlayerEntity.stopRiding();
            serverPlayerEntity.snapTo(pos.getX(),pos.getY(),pos.getZ());
        });
    }

    @Override
    public EventType<PitEvent> getType() {
        return TYPE;
    }
}
