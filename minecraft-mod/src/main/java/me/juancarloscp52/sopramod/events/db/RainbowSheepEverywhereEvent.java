package com.poc.sopramod.events.db;

import com.poc.sopramod.Variables;
import com.poc.sopramod.events.AbstractTimedEvent;
import com.poc.sopramod.events.EventType;

public class RainbowSheepEverywhereEvent extends AbstractTimedEvent {
    public static final EventType<RainbowSheepEverywhereEvent> TYPE = EventType.builder(RainbowSheepEverywhereEvent::new).build();

    @Override
    public void initClient() {
        Variables.rainbowSheepEverywhere = true;
    }

    @Override
    public void endClient() {
        super.endClient();
        Variables.rainbowSheepEverywhere = false;
    }

    @Override
    public EventType<RainbowSheepEverywhereEvent> getType() {
        return TYPE;
    }
}
