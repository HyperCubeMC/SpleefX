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
package io.github.spleefx.arena.spleef;

import com.google.gson.annotations.Expose;
import io.github.spleefx.arena.ModeType;
import io.github.spleefx.arena.api.ArenaType;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension.ExtensionType;
import io.github.spleefx.extension.standard.spleef.SpleefExtension;
import org.bukkit.Location;

public class SpleefArena extends GameArena {

    public static final SpleefExtension EXTENSION = ExtensionsManager.getExtension("spleef", ExtensionType.STANDARD, SpleefExtension.class);

    /**
     * Whether should snow melt in sudden death or when a player does not move out of a block
     */
    @Expose
    private boolean melt = false;

    /**
     * Creates a new spleef arena
     *
     * @param key               Key of the arena
     * @param displayName       Display name of the arena
     * @param regenerationPoint The regeneration point of the arena
     */
    public SpleefArena(String key, String displayName, Location regenerationPoint, ArenaType type) {
        super(key, displayName, regenerationPoint, type);
        post();
    }

    @Override
    public void post() {
        super.post();
        type = ModeType.SPLEEF;
        setEngine(new SpleefEngine(this));
        setExtension(EXTENSION);
    }

    public boolean isMelt() {
        return melt;
    }

    public void setMelt(boolean melt) {
        this.melt = melt;
    }
}
