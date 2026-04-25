package com.poc.sopramod.events.db;

import com.poc.sopramod.Variables;
import com.poc.sopramod.events.AbstractTimedEvent;
import com.poc.sopramod.events.EventType;

public class BouncyBlocksEvent extends AbstractTimedEvent {
    public static final EventType<BouncyBlocksEvent> TYPE = EventType.builder(BouncyBlocksEvent::new).build();

    @Override
    public void initClient() {
        Variables.bouncyBlocks = true;
    }

    @Override
    public void endClient() {
        Variables.bouncyBlocks = false;
        super.endClient();
    }

    @Override
    public EventType<BouncyBlocksEvent> getType() {
        return TYPE;
    }
}