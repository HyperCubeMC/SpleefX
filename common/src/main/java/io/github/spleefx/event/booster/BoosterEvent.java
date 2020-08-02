/*
 * * Copyright 2020 github.com/ReflxctionDev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.spleefx.event.booster;

import io.github.spleefx.economy.booster.BoosterInstance;
import org.bukkit.event.Event;

/**
 * Represents a booster event
 */
public abstract class BoosterEvent extends Event {

    /**
     * The booster involved in this event
     */
    private final BoosterInstance booster;

    /**
     * The default constructor is defined for cleaner code. This constructor
     * assumes the event is synchronous.
     */
    public BoosterEvent(BoosterInstance booster) {
        this.booster = booster;
    }

    /**
     * Returns the booster
     *
     * @return The booster
     */
    public BoosterInstance getBooster() {
        return booster;
    }

    /**
     * This constructor is used to explicitly declare an event as synchronous
     * or asynchronous.
     *
     * @param isAsync true indicates the event will fire asynchronously, false
     *                by default from default constructor
     */
    public BoosterEvent(boolean isAsync, BoosterInstance booster) {
        super(isAsync);
        this.booster = booster;
    }
}
