package com.poc.sopramod.events.db;

import com.poc.sopramod.events.AbstractInstantEvent;
import com.poc.sopramod.events.EventType;

public class NothingEvent extends AbstractInstantEvent {
    public static final EventType<NothingEvent> TYPE = EventType.builder(NothingEvent::new).build();

    @Override
    public EventType<NothingEvent> getType() {
        return TYPE;
    }
}
