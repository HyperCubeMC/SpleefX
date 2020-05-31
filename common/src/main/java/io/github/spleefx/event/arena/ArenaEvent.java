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
package io.github.spleefx.event.arena;

import io.github.spleefx.arena.api.ArenaEngine;
import io.github.spleefx.arena.api.GameArena;
import org.bukkit.event.Event;

/**
 * Represents an arena event
 */
public abstract class ArenaEvent extends Event {

    private GameArena arena;

    public ArenaEvent(GameArena arena) {
        this.arena = arena;
    }

    /**
     * Returns the arena involved in this event
     *
     * @return The arena in this event
     */
    public GameArena getArena() {
        return arena;
    }

    /**
     * Returns the engine of the arena in this event
     *
     * @return The arena engine in this event
     */
    public ArenaEngine getArenaEngine() {
        return arena.getEngine();
    }
}
