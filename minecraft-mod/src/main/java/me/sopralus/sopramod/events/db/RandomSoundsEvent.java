/*
 * Copyright (c) 2026 sopralus
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.poc.sopramod.events.db;

import com.poc.sopramod.Variables;
import com.poc.sopramod.events.AbstractTimedEvent;
import com.poc.sopramod.events.EventType;

public class RandomSoundsEvent extends AbstractTimedEvent {

    public static final EventType<RandomSoundsEvent> TYPE = EventType.builder(RandomSoundsEvent::new).build();

    @Override
    public void initClient() {
        Variables.randomSoundsChaos = true;
    }

    @Override
    public void endClient() {
        Variables.randomSoundsChaos = false;
        super.endClient();
    }

    @Override
    public short getDuration() {
        return (short) (super.getDuration() * 1.5);
    }

    @Override
    public EventType<RandomSoundsEvent> getType() {
        return TYPE;
    }
}
