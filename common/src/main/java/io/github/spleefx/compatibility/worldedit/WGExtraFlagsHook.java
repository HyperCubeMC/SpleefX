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
package io.github.spleefx.compatibility.worldedit;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import net.goldtreeservers.worldguardextraflags.WorldGuardExtraFlagsPlugin;
import net.goldtreeservers.worldguardextraflags.flags.Flags;
import net.goldtreeservers.worldguardextraflags.wg.WorldGuardUtils;
import net.goldtreeservers.worldguardextraflags.wg.wrappers.WorldGuardCommunicator;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Set;

public class WGExtraFlagsHook implements WorldGuardHook {

    /**
     * Returns whether can the specified player break the specified block
     *
     * @param player Player to test
     * @param block  Block to test
     * @return {@code true} if the block can be broken, {@code false} if otherwise.
     */
    @Override
    public boolean canBreak(Player player, Block block) {
        WorldGuardCommunicator communicator = WorldGuardExtraFlagsPlugin.getPlugin().getWorldGuardCommunicator();
        ApplicableRegionSet regions = communicator.getRegionContainer().createQuery().getApplicableRegions(block.getLocation());
        Set<Material> allow = WorldGuardUtils.queryValue(player, player.getWorld(), regions.getRegions(), Flags.ALLOW_BLOCK_BREAK);
        Set<Material> deny = WorldGuardUtils.queryValue(player, player.getWorld(), regions.getRegions(), Flags.DENY_BLOCK_BREAK);
        Material type = block.getType();
        boolean can = false;
        if (allow != null && allow.contains(type)) can = true;
        if (deny != null && deny.contains(type)) can = false;
        return can;
    }
}
