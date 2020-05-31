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
package io.github.spleefx.util.plugin;

import com.google.gson.annotations.Expose;

import java.util.concurrent.TimeUnit;

/**
 * Represents a duration
 */
public class Duration {

    /**
     * Represents the unit
     */
    @Expose
    private TimeUnit unit;

    /**
     * Represents the duration value
     */
    @Expose
    private long duration;

    private Duration(TimeUnit unit, long duration) {
        this.unit = unit;
        this.duration = duration;
    }

    /**
     * Returns the value of this duration in seconds
     *
     * @return The seconds
     */
    public long getSeconds() {
        return unit.toSeconds(duration);
    }

    @Override
    public String toString() { // 1 hour / 2 days / etc.
        return duration + " " + unit.name().toLowerCase().substring(0, unit.name().length() - 1) + (duration == 1 ? "" : "s");
    }

    /**
     * Creates a new duration
     *
     * @param unit  Unit of this duration
     * @param value Value of the duraton
     * @return The newly created duration
     */
    public static Duration of(TimeUnit unit, long value) {
        return new Duration(unit, value);
    }

}
