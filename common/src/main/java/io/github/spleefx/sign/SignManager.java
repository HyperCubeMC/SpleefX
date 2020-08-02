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
package io.github.spleefx.sign;

import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.util.PlaceholderUtil;

/**
 * A class for controlling signs for a specific arena
 */
public class SignManager {

    /**
     * Arena to update for
     */
    private final GameArena arena;

    public SignManager(GameArena arena) {
        this.arena = arena;
    }

    /**
     * Updates the arena signs
     */
    public void update() {
        try {
            arena.lines(
                    format(arena.getExtension().getSigns().get(0)),
                    format(arena.getExtension().getSigns().get(1)),
                    format(arena.getExtension().getSigns().get(2)),
                    format(arena.getExtension().getSigns().get(3))
            );
        } catch (NullPointerException e) {
            SpleefX.logger().warning("No sign definition in arena " + arena.getKey());
        }
    }

    /**
     * Formats the text by applying placeholders
     *
     * @param text Text to format
     * @return The formatted text
     */
    private String format(String text) {
        return PlaceholderUtil.all(text, arena);
    }

}