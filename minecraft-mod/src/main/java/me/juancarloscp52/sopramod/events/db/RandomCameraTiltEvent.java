package com.poc.sopramod.events.db;

import com.poc.sopramod.Variables;
import com.poc.sopramod.events.AbstractTimedEvent;
import com.poc.sopramod.events.EventCategory;
import com.poc.sopramod.events.EventType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;

public class RandomCameraTiltEvent extends AbstractTimedEvent {
    public static final EventType<RandomCameraTiltEvent> TYPE = EventType.builder(RandomCameraTiltEvent::new).category(EventCategory.CAMERA).disabledByAccessibilityMode().build();

    @Override
    public void initClient() {
        RandomSource random = Minecraft.getInstance().level.getRandom();
        Variables.cameraRoll = random.nextInt(360) + random.nextFloat();
    }

    @Override
    public void endClient() {
        Variables.cameraRoll = 0f;
        super.endClient();
    }

    @Override
    public EventType<RandomCameraTiltEvent> getType() {
        return TYPE;
    }
}
