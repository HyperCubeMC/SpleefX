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

import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.ArenaPlayer.ArenaPlayerState;
import io.github.spleefx.arena.api.ArenaType;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.sign.BlockLocation;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.game.Metas;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class SignListener implements Listener {

    private static final String LINE = "[spleef]";

    private static final String INVALID = ChatColor.RED + "Invalid arena";

    private static final String HEADER = Chat.colorize("&7[&cSpleefX&7]");

    private static final String FOOTER = Chat.colorize("&4Fetching arena...");

    private SpleefX plugin;

    public SignListener(SpleefX plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (!event.getPlayer().hasPermission("spleefx.create_arena_sign")) return;
        String header = event.getLine(0);
        if (header == null || !header.toLowerCase().equals(LINE)) return;
        event.setLine(0, HEADER);
        String arenaKey = event.getLine(1);

        // No line was inputted
        if (arenaKey == null) {
            event.setLine(1, INVALID);
            return;
        }

        GameArena arena = GameArena.getByKey(arenaKey);
        if (arena == null) {
            event.setLine(1, INVALID);
            return;
        }

        arena.getSigns().add(BlockLocation.at(event.getBlock().getLocation()));
        event.setLine(1, FOOTER);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        ArenaPlayer player;
        if (event.hasBlock() && block.getState() instanceof Sign) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getPlayer().getGameMode() == GameMode.CREATIVE)
                return;
            Sign sign = (Sign) block.getState();
            GameArena arena = GameArena.ARENAS.get().values().stream().filter(e -> e.getSigns().contains(BlockLocation.at(sign.getLocation()))).findFirst().orElse(null);
            if (arena == null) return; // Not an arena sign
            player = ArenaPlayer.adapt(event.getPlayer());
            if (arena.getSigns().contains(BlockLocation.at(sign.getLocation()))) {
                if (arena.getArenaType() == ArenaType.FREE_FOR_ALL)
                    arena.getEngine().join(player, null);
                else
                    arena.getEngine().join(player, null);
                event.setCancelled(true);
                if (arena.getEngine().getPlayerTeams().containsKey(ArenaPlayer.adapt(event.getPlayer()))) // To make sure the player joined
                    Metas.set(event.getPlayer(), "spleefx.arena.justjoined", new FixedMetadataValue(plugin, arena));
                return;
            }
        }
        if (event.hasBlock() && event.getClickedBlock().getState() instanceof Sign) return;
        player = ArenaPlayer.adapt(event.getPlayer());
        if (player.getState() != ArenaPlayerState.WAITING) return;
        if (player.getPlayer().hasMetadata("spleefx.arena.justjoined")) {
            player.getPlayer().removeMetadata("spleefx.arena.justjoined", plugin);
            return;
        }

        GameArena p = player.getCurrentArena();
        if (p.getExtension().getQuitItem().factory().create().isSimilar(event.getItem())) {
            p.getEngine().quit(player);
            event.setCancelled(true);
        }
    }
}
