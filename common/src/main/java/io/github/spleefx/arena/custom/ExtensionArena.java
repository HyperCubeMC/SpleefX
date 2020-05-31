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
package io.github.spleefx.arena.custom;

import com.google.gson.annotations.Expose;
import io.github.spleefx.arena.ModeType;
import io.github.spleefx.arena.SimpleArenaEngine;
import io.github.spleefx.arena.api.ArenaType;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import org.bukkit.Location;

/**
 * Represents an arena that belongs to a custom extension
 */
public class ExtensionArena extends GameArena {

    @Expose
    private String extension;

    private GameExtension gameExtension;

    /**
     * Creates a new spleef arena
     *
     * @param key               Key of the arena
     * @param displayName       Display name of the arena
     * @param regenerationPoint The regeneration point of the arena
     */
    public ExtensionArena(String key, String displayName, Location regenerationPoint, GameExtension extension, ArenaType type) {
        super(key, displayName, regenerationPoint, type);
        this.extension = extension.getKey();
        post();
    }

    @Override
    public void post() {
        super.post();
        this.type = ModeType.CUSTOM;
        setEngine(new SimpleArenaEngine<>(this));
        setExtension(getExtension());
    }

    @Override
    public GameExtension getExtension() {
        return gameExtension == null ? gameExtension = ExtensionsManager.getByKey(extension) : gameExtension;
    }
}