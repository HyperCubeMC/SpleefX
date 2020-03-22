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
package io.github.spleefx.listeners;

import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.ArenaPlayer.ArenaPlayerState;
import io.github.spleefx.arena.api.ArenaEngine;
import io.github.spleefx.arena.api.ArenaType;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.data.PlayerStatistic;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.util.plugin.PluginSettings;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;

public class ArenaListener implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) return;
        Player damaged = (Player) event.getEntity();
        ArenaPlayer p = ArenaPlayer.adapt(damaged);
        if (p.getState() == ArenaPlayerState.WAITING) {
            GameArena arena = p.getCurrentArena();
            if (arena.getExtension().getCancelledDamageInWaiting().contains(event.getCause()))
                event.setCancelled(true);
        } else if (p.getState() == ArenaPlayerState.IN_GAME) {
            GameArena arena = p.getCurrentArena();
            if (event instanceof EntityDamageByEntityEvent)
                handleTeamDamage(p, (EntityDamageByEntityEvent) event);
            if (arena.getExtension().getCancelledDamage().contains(event.getCause()))
                event.setCancelled(true);
            if (damaged.getHealth() - event.getDamage() < 1) {
                event.setCancelled(true);
                arena.getEngine().lose(p, arena.getEngine().getPlayerTeams().get(p));
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ArenaPlayer p = ArenaPlayer.adapt(event.getPlayer());
        if (p.getState() == ArenaPlayerState.WAITING || p.getState() == ArenaPlayerState.IN_GAME) {
            GameArena arena = p.getCurrentArena();
            if (arena.getExtension().isPreventItemDropping())
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        ArenaPlayer player = ArenaPlayer.adapt(event.getPlayer());
        if (player.getState() == ArenaPlayerState.WAITING || player.getState() == ArenaPlayerState.IN_GAME) {
            List<String> allowed = player.getCurrentArena().getExtension().getAllowedCommands();
            if (allowed.stream().anyMatch(event.getMessage()::startsWith)) return;
            if (player.getPlayer().hasPermission("spleefx.arena.command-exempt")) return;
            event.setCancelled(true);
            MessageKey.DISALLOWED_COMMAND.send(player.getPlayer(), null, null, null, null, event.getMessage(), null, -1, player.getCurrentArena().getExtension());
        }
    }

    private static void handleTeamDamage(ArenaPlayer p, EntityDamageByEntityEvent event) {
        if (p.getCurrentArena().getArenaType() == ArenaType.FREE_FOR_ALL) return;
        if (!(event.getDamager() instanceof Player)) return;
        Player damager = (Player) event.getDamager();
        ArenaPlayer d = ArenaPlayer.adapt(damager);
        if (d.getCurrentArena().equals(p.getCurrentArena())) {
            ArenaEngine a = d.getCurrentArena().getEngine();
            if (a.getPlayerTeams().get(d).equals(a.getPlayerTeams().get(p)) && (boolean) PluginSettings.ARENA_CANCEL_TEAM_DAMAGE.get())
                event.setCancelled(true);
        }
    }

    public static class WGListener implements Listener {

        private SpleefX plugin;

        public WGListener(SpleefX plugin) {
            this.plugin = plugin;
        }

        @EventHandler(priority = EventPriority.HIGHEST) // we need to override it to allow games to run
        public void onBreakBlock(BreakBlockEvent event) {
            if (event.getCause().getRootCause() instanceof Player) {
                Player player = event.getCause().getFirstPlayer();
                ArenaPlayer p = ArenaPlayer.adapt(event.getCause().getFirstPlayer());
                for (Block block : event.getBlocks()) {
                    event.setAllowed(CompatibilityHandler.getWorldGuardHook().canBreak(player, block));
                    if (p.getState() == ArenaPlayerState.IN_GAME) {
                        if (event.getResult() != Result.DENY) {
                            GameArena arena = p.getCurrentArena();
                            if (!arena.getDropMinedBlocks()) {
                                Collection<ItemStack> oldDrops = block.getDrops();
                                block.setType(Material.AIR);
                                if (arena.getExtension().isGiveDroppedItems())
                                    p.getPlayer().getInventory().addItem(oldDrops.toArray(new ItemStack[0]));
                            }
                            plugin.getDataProvider().add(PlayerStatistic.BLOCKS_MINED, p.getPlayer(), arena.getExtension(), 1);
                        }
                    }
                }
            }
        }
    }

    public static class BlockBreakListener implements Listener {

        private SpleefX plugin;

        public BlockBreakListener(SpleefX plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onBlockBreak(BlockBreakEvent event) {
            ArenaPlayer p = ArenaPlayer.adapt(event.getPlayer());
            if (p.getState() == ArenaPlayerState.IN_GAME) {
                GameArena arena = p.getCurrentArena();
                if (!arena.getDropMinedBlocks()) {
                    Collection<ItemStack> oldDrops = event.getBlock().getDrops();
                    event.getBlock().setType(Material.AIR);
                    if (arena.getExtension().isGiveDroppedItems())
                        p.getPlayer().getInventory().addItem(oldDrops.toArray(new ItemStack[0]));
                }
                plugin.getDataProvider().add(PlayerStatistic.BLOCKS_MINED, p.getPlayer(), arena.getExtension(), 1);
            }
        }
    }
}