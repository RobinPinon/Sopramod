/**
 * @author Kanawanagasaki
 */

package com.poc.sopramod.events.db;

import com.poc.sopramod.Sopramod;
import com.poc.sopramod.SopramodTags.BlockTags;
import com.poc.sopramod.events.AbstractInstantEvent;
import com.poc.sopramod.events.EventType;
import net.minecraft.world.level.block.Blocks;

public class PlaceCobwebBlockEvent extends AbstractInstantEvent {
    public static final EventType<PlaceCobwebBlockEvent> TYPE = EventType.builder(PlaceCobwebBlockEvent::new).build();

    @Override
    public void init() {
        for(var serverPlayerEntity : Sopramod.getInstance().eventHandler.getActivePlayers()) {
            if(serverPlayerEntity.level().getBlockState(serverPlayerEntity.blockPosition()).is(BlockTags.NOT_REPLACED_BY_EVENTS))
                continue;
            serverPlayerEntity.level().setBlockAndUpdate(serverPlayerEntity.blockPosition(), Blocks.COBWEB.defaultBlockState());
        }
    }

    @Override
    public EventType<PlaceCobwebBlockEvent> getType() {
        return TYPE;
    }
}
