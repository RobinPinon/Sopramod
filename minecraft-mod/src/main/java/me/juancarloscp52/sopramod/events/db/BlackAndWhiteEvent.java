package com.poc.sopramod.events.db;

import com.poc.sopramod.Variables;
import com.poc.sopramod.events.AbstractTimedEvent;
import com.poc.sopramod.events.EventCategory;
import com.poc.sopramod.events.EventType;

public class BlackAndWhiteEvent extends AbstractTimedEvent {
    public static final EventType<BlackAndWhiteEvent> TYPE = EventType.builder(BlackAndWhiteEvent::new).category(EventCategory.SHADER).build();

    @Override
    public void initClient() {
        Variables.blackAndWhite = true;
    }

    @Override
    public void endClient() {
        Variables.blackAndWhite = false;
        super.endClient();
    }

    @Override
    public short getDuration() {
        return (short) (super.getDuration()*1.5);
    }

    @Override
    public EventType<BlackAndWhiteEvent> getType() {
        return TYPE;
    }
}
