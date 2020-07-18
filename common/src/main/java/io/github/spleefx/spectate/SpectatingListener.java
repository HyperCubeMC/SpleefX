/*
 * * Copyright 2019-2020 github.com/ReflxctionDev
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
package io.github.spleefx.spectate;

import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.ArenaPlayer.ArenaPlayerState;
import io.github.spleefx.event.ability.PlayerDoubleJumpEvent;
import io.github.spleefx.util.plugin.Protocol;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static io.github.spleefx.SpleefX.getPlugin;
import static io.github.spleefx.SpleefX.getSpectatorSettings;

public class SpectatingListener implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (isSpectating(event.getEntity())) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (isSpectating(event.getTarget())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isSpectating(event.getDamager())) return;
        if (event.getEntityType() != EntityType.PLAYER || isSpectating(event.getEntity())) {
            event.setCancelled(true);
            return;
        }
        ArenaPlayer target = ArenaPlayer.adapt((Player) event.getEntity());
        if (target.getState() == ArenaPlayerState.IN_GAME) {
            Player spectator = ((Player) event.getDamager());
            spectator.setGameMode(GameMode.SPECTATOR);
            spectator.setSpectatorTarget(event.getEntity());
            Bukkit.getPluginManager().callEvent(new PlayerSpectateAnotherEvent(spectator, target.getPlayer(), target.getCurrentArena()));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (isSpectating(event.getPlayer())) {
            event.setCancelled(true);
            if (event.getItem() == null) return;
            ArenaPlayer arenaPlayer = ArenaPlayer.adapt(event.getPlayer());
            ItemStack item = event.getItem();
            if (item.isSimilar(getSpectatorSettings().getSpectateItem().factory().create())) {
                if (event.getPlayer().getGameMode() == GameMode.SPECTATOR)
                    event.getPlayer().setSpectatorTarget(null);
                new SpectatePlayerMenu(arenaPlayer.getCurrentArena(), false).display(event.getPlayer());
            } else if (item.isSimilar(getSpectatorSettings().getExitSpectatingItem().factory().create())) {
                arenaPlayer.getCurrentArena().getEngine().quit(arenaPlayer, false);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryCreative(InventoryCreativeEvent event) {
        if (isSpectating(event.getWhoClicked())) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerSpectateAnother(@NotNull PlayerSpectateAnotherEvent event) {
        if (getSpectatorSettings().enabled) {
            getSpectatorSettings().getTitleOnSpectate().display(event.getSpectator(), event.getTarget());
            getSpectatorSettings().getSpectatingActionBar().display(event.getSpectator(), event.getTarget());
        } else {
            Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                ArenaPlayer arenaPlayer = ArenaPlayer.adapt(event.getSpectator());
                arenaPlayer.getCurrentArena().getEngine().quit(arenaPlayer, false);
            }, 1);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDoubleJump(@NotNull PlayerDoubleJumpEvent event) {
        if (isSpectating(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().setAllowFlight(true);
            event.getPlayer().setFlying(true);
        }
    }

    @EventHandler(ignoreCancelled = true) public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (isSpectating(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true) public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if (isSpectating(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true) public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (isSpectating(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (Protocol.PROTOCOL == 8) return; // 1.8.x
        if (event.getCause() == TeleportCause.SPECTATE)
            if (event.getPlayer().getSpectatorTarget() == null)
                Bukkit.getPluginManager().callEvent(new PlayerExitSpectateEvent(event.getPlayer()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerExitSpectate(PlayerExitSpectateEvent event) {
        if (isSpectating(event.getPlayer())) {
            event.getPlayer().setGameMode(GameMode.SURVIVAL);
            event.getPlayer().setAllowFlight(true);
            event.getPlayer().setFlying(true);
        }
    }

    public static boolean isSpectating(Entity entity) {
        return getSpectatorSettings().enabled && entity instanceof Player && ArenaPlayer.adapt((Player) entity).isSpectating();
    }

    public static class PickupListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void onPlayerPickupArrow(PlayerPickupArrowEvent event) {
            if (isSpectating(event.getPlayer())) event.setCancelled(true);
        }

        @EventHandler(ignoreCancelled = true)
        public void onEntityPickupItem(EntityPickupItemEvent event) {
            if (isSpectating(event.getEntity())) event.setCancelled(true);
        }

    }

}