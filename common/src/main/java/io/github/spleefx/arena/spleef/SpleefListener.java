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

import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.compatibility.CompatibilityHandler;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class SpleefListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntityType() != EntityType.SNOWBALL) return;
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        Player p = (Player) event.getEntity().getShooter();
        ArenaPlayer player = ArenaPlayer.adapt(p);
        if (player.getCurrentArena() instanceof SpleefArena)
            event.setCancelled(CompatibilityHandler.either(() -> p.getInventory().getItemInMainHand(), () -> p.getItemInHand()).hasItemMeta() && SpleefArena.EXTENSION.getSnowballSettings().getAllowThrowing());
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        ArenaPlayer player = ArenaPlayer.adapt(((Player) event.getEntity().getShooter()));
        if (!(player.getCurrentArena() instanceof SpleefArena)) return;
        Block hitBlock = CompatibilityHandler.getHitBlock(player.getCurrentArena(), event);
        if (hitBlock == null) return;
        if (SpleefArena.EXTENSION.getSnowballSettings().getThrownSnowballsRemoveHitBlocks().contains(hitBlock.getType())) {
            hitBlock.setType(Material.AIR);
        }
    }
}
