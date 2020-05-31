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
package io.github.spleefx.arena;

import io.github.spleefx.arena.api.BaseArenaEngine;
import io.github.spleefx.arena.api.GameArena;

/**
 * Represents a basic engine which derives all its functionality from extensions.
 *
 * @param <R> Arena type
 * @see BaseArenaEngine
 */
public class SimpleArenaEngine<R extends GameArena> extends BaseArenaEngine<R> {

    /**
     * Creates an engine for the specified arena
     *
     * @param arena Arena to create for
     */
    public SimpleArenaEngine(R arena) {
        super(arena);
    }
}
