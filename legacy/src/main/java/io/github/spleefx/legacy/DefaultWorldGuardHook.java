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
package io.github.spleefx.legacy;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import io.github.spleefx.compatibility.worldedit.WorldGuardHook;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class DefaultWorldGuardHook implements WorldGuardHook {

    /**
     * Returns whether can the specified player break the specified block
     *
     * @param player Player to test
     * @param block  Block to test
     * @return {@code true} if the block can be broken, {@code false} if otherwise.
     */
    @Override
    public boolean canBreak(Player player, Block block) {
        RegionContainer container = WorldGuardPlugin.inst().getRegionContainer();
        RegionQuery query = container.createQuery();
        org.bukkit.Location bukkitLoc = block.getLocation();
        return query.testState(bukkitLoc, player, DefaultFlag.BLOCK_BREAK);
    }
}
