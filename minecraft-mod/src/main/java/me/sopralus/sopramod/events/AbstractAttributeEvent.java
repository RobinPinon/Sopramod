package com.poc.sopramod.events;

import com.poc.sopramod.Sopramod;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import java.util.List;

public abstract class AbstractAttributeEvent extends AbstractTimedEvent {
    public record ActiveModifier(Holder<Attribute> attribute, AttributeModifier modifier) {
    }

    private List<ActiveModifier> modifiers = List.of();

    protected abstract List<ActiveModifier> getModifiers();

    @Override
    public void init() {
        modifiers = getModifiers();
        Sopramod.getInstance().eventHandler.getActivePlayers().forEach(this::startPlayer);
    }

    private void startPlayer(ServerPlayer player) {
        modifiers.forEach(active -> player.getAttribute(active.attribute()).addTransientModifier(active.modifier()));
    }

    @Override
    public void end() {
        Sopramod.getInstance().eventHandler.getActivePlayers().forEach(this::endPlayer);
        modifiers = List.of();
        super.end();
    }

    @Override
    public void endPlayer(ServerPlayer player) {
        modifiers.forEach(active -> player.getAttribute(active.attribute()).removeModifier(active.modifier().id()));
    }

    @Override
    public void tick() {
        if (getTickCount() % 20 == 0) {
            for (final ServerPlayer player : Sopramod.getInstance().eventHandler.getActivePlayers()) {
                for (final ActiveModifier active : modifiers) {
                    final AttributeInstance attributeInstance = player.getAttribute(active.attribute());
                    if (attributeInstance.getModifier(active.modifier().id()) == null) {
                        attributeInstance.addTransientModifier(active.modifier());
                    }
                }
            }
        }
        super.tick();
    }
}
