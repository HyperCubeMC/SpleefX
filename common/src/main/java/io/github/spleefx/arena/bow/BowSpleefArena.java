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
package io.github.spleefx.arena.bow;

import io.github.spleefx.arena.ModeType;
import io.github.spleefx.arena.api.ArenaType;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.arena.api.GameTask;
import io.github.spleefx.extension.standard.bowspleef.BowSpleefExtension;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BowSpleefArena extends GameArena {

    private Map<UUID, Player> damageMap;

    /**
     * Creates a new spleef arena
     *
     * @param key               Key of the arena
     * @param displayName       Display name of the arena
     * @param regenerationPoint The regeneration point of the arena
     */
    public BowSpleefArena(String key, String displayName, Location regenerationPoint, ArenaType type) {
        super(key, displayName, regenerationPoint, type);
        post();
    }

    @Override
    public void post() {
        super.post();
        type = ModeType.BOW_SPLEEF;
        setEngine(new BowSpleefEngine(this));
        setExtension(BowSpleefExtension.EXTENSION);
        damageMap = new HashMap<>();
        ((BowSpleefEngine) getEngine()).registerEndTask(new ClearTask());
    }

    public Map<UUID, Player> getDamageMap() {
        return damageMap;
    }

    class ClearTask extends GameTask {

        public ClearTask() {
            super(Phase.AFTER);
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            if (damageMap != null && !damageMap.isEmpty())
                damageMap.clear();
        }
    }

}