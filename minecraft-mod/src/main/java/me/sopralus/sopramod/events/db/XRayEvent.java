package com.poc.sopramod.events.db;

import com.poc.sopramod.Variables;
import com.poc.sopramod.events.AbstractTimedEvent;
import com.poc.sopramod.events.EventType;
import net.minecraft.client.Minecraft;

public class XRayEvent extends AbstractTimedEvent {
    public static final EventType<XRayEvent> TYPE = EventType.builder(XRayEvent::new).build();

    @Override
    public void initClient() {
        Variables.xrayActive = true;

        // Rerender the world because of the caching
        var client = Minecraft.getInstance();
        client.levelRenderer.allChanged();
    }

    @Override
    public void endClient() {
        Variables.xrayActive = false;

        // Rerender the world because of the caching
        var client = Minecraft.getInstance();
        client.levelRenderer.allChanged();

        super.endClient();
    }

    @Override
    public EventType<XRayEvent> getType() {
        return TYPE;
    }
}
