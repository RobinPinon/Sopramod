/*
 * Copyright (c) 2026 sopralus
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.poc.sopramod.events.db;

import com.poc.sopramod.Sopramod;
import com.poc.sopramod.Variables;
import com.poc.sopramod.events.AbstractTimedEvent;
import com.poc.sopramod.events.EventCategory;
import com.poc.sopramod.events.EventType;

public class TimerSpeedHalfEvent extends AbstractTimedEvent {
    public static final EventType<TimerSpeedHalfEvent> TYPE = EventType.builder(TimerSpeedHalfEvent::new).category(EventCategory.TIMER).build();

    @Override
    public void initClient() {
        Variables.timerMultiplier = 0.5f;
    }

    @Override
    public void endClient() {
        Variables.timerMultiplier = 1;
        super.endClient();
    }

    @Override
    public void init() {
        Variables.timerMultiplier = 0.5f;
    }

    @Override
    public void end() {
        Variables.timerMultiplier = 1;
        Sopramod.getInstance().eventHandler.resetTimer();
        super.end();
    }

    @Override
    public short getDuration() {
        return (short) (super.getDuration() * 3.5f);
    }

    @Override
    public EventType<TimerSpeedHalfEvent> getType() {
        return TYPE;
    }
}
