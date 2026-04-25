package com.poc.sopramod.events.db;

import com.poc.sopramod.Variables;
import com.poc.sopramod.events.AbstractTimedEvent;
import com.poc.sopramod.events.EventType;

public class OnePunchEvent extends AbstractTimedEvent {
    public static final EventType<OnePunchEvent> TYPE = EventType.builder(OnePunchEvent::new).build();

    @Override
    public void init() {
        Variables.shouldLaunchEntity++;
        Variables.isOnePunchActivated++;
    }

    @Override
    public void end() {
        Variables.shouldLaunchEntity--;
        Variables.isOnePunchActivated--;
        super.end();
    }

    @Override
    public EventType<OnePunchEvent> getType() {
        return TYPE;
    }
}
