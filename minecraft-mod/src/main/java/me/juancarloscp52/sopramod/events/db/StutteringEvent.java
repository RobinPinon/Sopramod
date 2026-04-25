package com.poc.sopramod.events.db;

import com.poc.sopramod.Variables;
import com.poc.sopramod.events.AbstractTimedEvent;
import com.poc.sopramod.events.EventType;

public class StutteringEvent extends AbstractTimedEvent {
    public static final EventType<StutteringEvent> TYPE = EventType.builder(StutteringEvent::new).disabledByAccessibilityMode().build();

    @Override
    public void initClient() {
        Variables.stuttering = true;
    }

    @Override
    public void endClient() {
        super.endClient();
        Variables.stuttering = false;
    }

    @Override
    public EventType<StutteringEvent> getType() {
        return TYPE;
    }
}
