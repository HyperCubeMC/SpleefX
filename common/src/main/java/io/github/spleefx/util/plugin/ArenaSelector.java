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

import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.extension.GameExtension;

import java.util.Comparator;
import java.util.function.Predicate;

public class ArenaSelector {

    private static final Comparator<GameArena> BY_COUNT = (a, b) -> Integer.compare(b.getEngine().getPlayerTeams().size(), a.getEngine().getPlayerTeams().size());

    private static final Predicate<GameArena> ALLOWED_TO_JOIN = arena -> !arena.getEngine().isFull() && arena.getEngine().getArenaStage().isPlayable();

    /**
     * Picks an arena to join for the extension mode. The returned arena has the most players and is not full
     *
     * @param mode Mode to pick
     * @param <R>  The arena reference
     * @return The picked arena, or {@code null} if no arena is found
     */
    @SuppressWarnings("unchecked")
    public static <R extends GameArena> R pick(GameExtension mode) {
        return (R) GameArena.ARENAS.get()
                .values()
                .stream()
                .filter(a -> (a.getExtension().getKey().equals(mode.getKey()) && ALLOWED_TO_JOIN.test(a)))
                .min(BY_COUNT)
                .orElse(null);
    }
}
