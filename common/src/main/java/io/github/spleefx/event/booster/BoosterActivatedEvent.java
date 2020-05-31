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
import org.bukkit.event.HandlerList;

public class BoosterActivatedEvent extends BoosterEvent {

    /**
     * The default constructor is defined for cleaner code. This constructor
     * assumes the event is synchronous.
     *
     * @param booster
     */
    public BoosterActivatedEvent(BoosterInstance booster) {
        super(booster);
    }

    @Override public HandlerList getHandlers() {
        return null;
    }
}
