/**
 * @author Kanawanagasaki
 */

package com.poc.sopramod.events.db;

import com.poc.sopramod.Sopramod;
import com.poc.sopramod.SopramodTags.BlockTags;
import com.poc.sopramod.events.AbstractInstantEvent;
import com.poc.sopramod.events.EventType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public class ZeusUltEvent extends AbstractInstantEvent {
    public static final EventType<ZeusUltEvent> TYPE = EventType.builder(ZeusUltEvent::new).build();

    @Override
    public void init() {
        for (var serverPlayerEntity : Sopramod.getInstance().eventHandler.getActivePlayers()) {
            var world = serverPlayerEntity.level();
            var playerPos = serverPlayerEntity.blockPosition();
            var pos = world.getHeightmapPos(Types.WORLD_SURFACE, playerPos);
            for (int iy = playerPos.getY(); iy < pos.getY(); iy++)
                if (!world.getBlockState(pos).is(BlockTags.NOT_REPLACED_BY_ZEUS_ULT))
                    world.destroyBlock(playerPos.atY(iy), true);
            var lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, world);
            lightning.setPos(playerPos.getX(), playerPos.getY(), playerPos.getZ());
            world.addFreshEntity(lightning);
        }
    }

    @Override
    public EventType<ZeusUltEvent> getType() {
        return TYPE;
    }
}
