/**
 * @author Kanawanagasaki
 */

package com.poc.sopramod.events.db;

import com.poc.sopramod.Sopramod;
import com.poc.sopramod.SopramodTags.BlockTags;
import com.poc.sopramod.events.AbstractTimedEvent;
import com.poc.sopramod.events.EventCategory;
import com.poc.sopramod.events.EventType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;

public class MiningSightEvent extends AbstractTimedEvent {
    public static final EventType<MiningSightEvent> TYPE = EventType.builder(MiningSightEvent::new).category(EventCategory.SIGHT).build();

    @Override
    public void tick() {
        if(tickCount%2==0){
            for (var serverPlayerEntity : Sopramod.getInstance().eventHandler.getActivePlayers()) {
                var hitRes = serverPlayerEntity.pick(64, 1, false);
                if (hitRes.getType() == Type.BLOCK) {
                    var blockHitRes = (BlockHitResult) hitRes;
                    var blockPos = blockHitRes.getBlockPos();
                    var world = serverPlayerEntity.level();
                    if (!world.getBlockState(blockPos).is(BlockTags.NOT_REPLACED_BY_EVENTS))
                        world.destroyBlock(blockPos, true, serverPlayerEntity);
                }
            }
        }

        super.tick();
    }

    @Override
    public EventType<MiningSightEvent> getType() {
        return TYPE;
    }
}
