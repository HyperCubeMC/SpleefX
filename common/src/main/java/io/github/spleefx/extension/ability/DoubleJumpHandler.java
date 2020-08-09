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
package io.github.spleefx.extension.ability;

import com.google.gson.annotations.Expose;
import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.ArenaPlayer.ArenaPlayerState;
import io.github.spleefx.arena.api.BaseArenaEngine;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.event.ability.PlayerDoubleJumpEvent;
import io.github.spleefx.extension.ItemHolder;
import io.github.spleefx.util.game.VectorHolder;
import io.github.spleefx.util.plugin.DelayExecutor;
import io.github.spleefx.util.plugin.DelayExecutor.DelayData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemFlag;

import java.util.Collections;
import java.util.HashMap;

/**
 * A listener for handling double jumps
 */
public class DoubleJumpHandler implements Listener {
    private SpleefX plugin;

    /**
     * The delay handler
     */
    private DelayExecutor<GameAbility> delayExecutor;

    private static HashMap<Player, Boolean> isPlayerDoubleJumpingMap = new HashMap<Player, Boolean>();

    /**
     * Creates a new handler
     *
     * @param delayExecutor The delay handler
     */
    public DoubleJumpHandler(SpleefX plugin, DelayExecutor<GameAbility> delayExecutor) {
        this.plugin = plugin;
        this.delayExecutor = delayExecutor;
    }

    /**
     * Fired when a player toggles their flight in a game
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.SPECTATOR || event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;
        ArenaPlayer player = ArenaPlayer.adapt(event.getPlayer());
        if (player.getCurrentArena() == null) return;
        if (player.getCurrentArena().getExtension().getDoubleJumpSettings().isEnabled() && event.isFlying()) {
            doubleJump(player.getCurrentArena(), event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem()) return;
        ArenaPlayer player = ArenaPlayer.adapt(event.getPlayer());
        if (player.getCurrentArena() == null) return;
        DataHolder settings = player.getCurrentArena().getExtension().getDoubleJumpSettings();
        if (settings.isEnabled() && settings.getDoubleJumpItems().isEnabled() && event.getItem().isSimilar(settings.getDoubleJumpItems().onAvailable.factory().create())) {
            doubleJump(player.getCurrentArena(), event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player player = event.getPlayer();
            if (isPlayerDoubleJumping(player) && player.isOnGround()) {
                isPlayerDoubleJumpingMap.put(player, false);
            }
        }, 5L);
    }

    /**
     * Double-jumps the player in the specified arena
     *
     * @param arena  Arena to double jump in
     * @param player Player to double jump for
     */
    public void doubleJump(GameArena arena, Player player) {
        try {
            ArenaPlayer ap = ArenaPlayer.adapt(player);
            BaseArenaEngine<? extends GameArena> engine = (BaseArenaEngine<? extends GameArena>) arena.getEngine();
            if (engine.getAbilityCount().getOrDefault(player.getUniqueId(), BaseArenaEngine.DEFAULT_MAP).getOrDefault(GameAbility.DOUBLE_JUMP, 0) <= 0) {
                if (ap.isSpectating()) {
                    player.setAllowFlight(true);
                    player.setFlying(true);
                }
                return; // Player has no more double jumps
            }
            if (delayExecutor.hasDelay(player, GameAbility.DOUBLE_JUMP)) return;
            int v = ap.isSpectating() ? engine.getAbilityCount().get(player.getUniqueId()).get(GameAbility.DOUBLE_JUMP) : GameAbility.DOUBLE_JUMP.reduceAbility(engine.getAbilityCount().get(player.getUniqueId()));
            PlayerDoubleJumpEvent event = new PlayerDoubleJumpEvent(player, v, arena);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;
            isPlayerDoubleJumpingMap.put(player, true);
            player.setVelocity(arena.getExtension().getDoubleJumpSettings().getLaunchVelocity().getVector(player));
            if (!ap.isSpectating())
                player.setAllowFlight(false);
            engine.addDoubleJumpItems(ap, false);
            if (arena.getExtension().getDoubleJumpSettings().getPlaySoundOnJump() != null)
                player.playSound(player.getLocation(), arena.getExtension().getDoubleJumpSettings().getPlaySoundOnJump(), 1, 1);
            delayExecutor.setDelay(player, GameAbility.DOUBLE_JUMP, new DelayData(arena.getExtension().getDoubleJumpSettings().getCooldownBetween()).setOnFinish((p) -> {
                if (p.getPlayer() != null && v > 0) {
                    ArenaPlayer arenaPlayer = ArenaPlayer.adapt(p.getPlayer());
                    if (arenaPlayer.getState() == ArenaPlayerState.IN_GAME) {
                        p.getPlayer().setAllowFlight(true);
                        engine.addDoubleJumpItems(arenaPlayer, true);
                    } else if (arenaPlayer.isSpectating()) {
                        p.getPlayer().setAllowFlight(true);
                    }
                }
            }));
        } catch (ClassCastException e) {
            SpleefX.logger().warning("Arena type " + arena.getExtension().getDisplayName() + " has an unsupported arena engine implementation. Double jumps will not work.");
        }
    }

    /**
     * Checks if a player is double jumping
     * @param player - The player to check
     * @return Boolean - Whether the player is double jumping or not
     */
    public static boolean isPlayerDoubleJumping(Player player) {
        if (isPlayerDoubleJumpingMap.get(player) != null) {
            return isPlayerDoubleJumpingMap.get(player);
        }
        // Not in the map if we're at this point, player is not double jumping, so return false
        return false;
    }

    /**
     * A POJO for holding double jump data
     */
    public static class DataHolder {

        @Expose
        private boolean enabled = true;

        @Expose
        private int defaultAmount = 5;

        @Expose
        private int cooldownBetween = 2;

        @Expose
        private Sound playSoundOnJump = CompatibilityHandler.either(() -> Sound.valueOf("ENTITY_WITHER_SHOOT"), () -> Sound.valueOf("WITHER_SHOOT"));

        @Expose
        private DoubleJumpItems doubleJumpItems = new DoubleJumpItems(
                true, 2,
                new ItemHolder()
                        .setType("feather")
                        .setCount(1)
                        .setDisplayName("&aDouble Jump")
                        .setItemFlags(Collections.singletonList(ItemFlag.HIDE_ENCHANTS)),
                new ItemHolder()
                        .setType("feather")
                        .setCount(1)
                        .setDisplayName("&cDouble Jump")
                        .setItemFlags(Collections.singletonList(ItemFlag.HIDE_ENCHANTS))
        );

        @Expose
        private VectorHolder launchVelocity = new VectorHolder(0, 1, 0);

        public boolean isEnabled() {
            return enabled;
        }

        public int getDefaultAmount() {
            return defaultAmount;
        }

        public int getCooldownBetween() {
            return cooldownBetween;
        }

        public Sound getPlaySoundOnJump() {
            return playSoundOnJump;
        }

        public VectorHolder getLaunchVelocity() {
            return launchVelocity;
        }

        public DoubleJumpItems getDoubleJumpItems() {
            return doubleJumpItems;
        }
    }

    public static class DoubleJumpItems {

        @Expose
        private boolean enabled;

        @Expose
        private int slot;

        @Expose
        private ItemHolder onAvailable;

        @Expose
        private ItemHolder onUnavailable;

        public DoubleJumpItems(boolean enabled, int slot, ItemHolder onAvailable, ItemHolder onUnavailable) {
            this.enabled = enabled;
            this.slot = slot;
            this.onAvailable = onAvailable;
            this.onUnavailable = onUnavailable;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public int getSlot() {
            return slot;
        }

        public ItemHolder getAvailable() {
            return onAvailable;
        }

        public ItemHolder getUnavailable() {
            return onUnavailable;
        }
    }

}