/**
 * @author Kanawanagasaki
 */

package com.poc.sopramod.events.db;

import com.poc.sopramod.Sopramod;
import com.poc.sopramod.SopramodTags.EntityTypeTags;
import com.poc.sopramod.events.AbstractTimedEvent;
import com.poc.sopramod.events.EventCategory;
import com.poc.sopramod.events.EventType;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;

public class DeathSightEvent extends AbstractTimedEvent {
    public static final EventType<DeathSightEvent> TYPE = EventType.builder(DeathSightEvent::new).category(EventCategory.SIGHT).build();

    @Override
    public void tick() {
        for (var serverPlayerEntity : Sopramod.getInstance().eventHandler.getActivePlayers()) {
            var rayVector = serverPlayerEntity.getLookAngle().normalize().scale(64d);
            var fromVector = serverPlayerEntity.getEyePosition();
            var toVector = fromVector.add(rayVector);
            var box = new AABB(serverPlayerEntity.position().add(64, 64, 64),
                    serverPlayerEntity.position().subtract(64, 64, 64));
            var hitRes = ProjectileUtil.getEntityHitResult(serverPlayerEntity, fromVector, toVector, box, x -> true, 2048);
            if (hitRes != null) {
                var difficulty = serverPlayerEntity.level().getDifficulty();
                var dmg = difficulty == Difficulty.HARD ? 3 : difficulty == Difficulty.NORMAL ? 5 : 7;
                var entity = hitRes.getEntity();
                if (entity instanceof LivingEntity && !entity.getType().is(EntityTypeTags.DO_NOT_DAMAGE))
                    entity.hurt(entity.damageSources().playerAttack(serverPlayerEntity), dmg);
            }
        }

        super.tick();
    }

    @Override
    public EventType<DeathSightEvent> getType() {
        return TYPE;
    }
}
