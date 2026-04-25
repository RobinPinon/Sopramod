/**
 * @author Kanawanagasaki
 */

package com.poc.sopramod.events.db;

import com.poc.sopramod.Sopramod;
import com.poc.sopramod.events.AbstractInstantEvent;
import com.poc.sopramod.events.EventCategory;
import com.poc.sopramod.events.EventType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class InvisiblePlayerEvent extends AbstractInstantEvent {
    public static final EventType<InvisiblePlayerEvent> TYPE = EventType.builder(InvisiblePlayerEvent::new).category(EventCategory.INVISIBILITY).build();

    @Override
    public void init() {
        for (var serverPlayerEntity : Sopramod.getInstance().eventHandler.getActivePlayers()) {
            var effect = new MobEffectInstance(MobEffects.INVISIBILITY,
                    Sopramod.getInstance().settings.baseEventDuration, 1, true, false);
            serverPlayerEntity.addEffect(effect);
        }
    }

    @Override
    public EventType<InvisiblePlayerEvent> getType() {
        return TYPE;
    }
}
