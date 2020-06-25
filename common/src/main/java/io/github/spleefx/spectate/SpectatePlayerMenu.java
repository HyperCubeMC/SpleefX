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

import com.google.gson.annotations.Expose;
import io.github.spleefx.arena.api.BaseArenaEngine;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.util.PlaceholderUtil;
import io.github.spleefx.util.item.ItemFactory;
import io.github.spleefx.util.menu.Button;
import io.github.spleefx.util.menu.GameMenu;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static io.github.spleefx.SpleefX.getSpectatorSettings;

public class SpectatePlayerMenu extends GameMenu {

    /**
     * Create a new spectate player menu
     *
     * @param arena Arena to create for
     */
    public SpectatePlayerMenu(GameArena arena) {
        super(getSpectatorSettings().getSpectatePlayerMenu().getTitle(), getAppropriateSize(arena.getEngine().getAlive().size()));
        cancelAllClicks = true;
        List<Player> alivePlayers = arena.getEngine().getAlive();
        for (AtomicInteger i = new AtomicInteger(); i.get() < alivePlayers.size(); i.getAndIncrement()) {
            Player player = alivePlayers.get(i.get());
            setButton(new Button(i.get(), ((BaseArenaEngine<?>) arena.getEngine()).playerHeads.get(player.getUniqueId()))
                    .addAction(Button.CLOSE_INVENTORY)
                    .addAction(e -> {
                        Player spectator = ((Player) e.getWhoClicked());
                        spectator.setGameMode(GameMode.SPECTATOR);
                        spectator.setSpectatorTarget(player.getPlayer());
                        Bukkit.getPluginManager().callEvent(new PlayerSpectateAnotherEvent(spectator, player.getPlayer(), arena));

                    }));
        }
    }

    public static class MenuData {

        @Expose
        private String title = "&2Spectate players";

        @Expose public PlayerHeadInfo playerHeadInfo = new PlayerHeadInfo();

        public String getTitle() {
            return title;
        }
    }

    public static class PlayerHeadInfo {

        @Expose
        private String displayName = "&2{player}";

        @Expose
        private List<String> lore;

        public CompletableFuture<ItemStack> apply(Player player) {
            CompletableFuture<ItemStack> headFuture = CompatibilityHandler.getMaterialCompatibility().skull(player);
            return headFuture.thenApply(head -> ItemFactory
                    .create(head)
                    .setName(PlaceholderUtil.all(displayName, player))
                    .setLore(lore.stream().map(l -> PlaceholderUtil.all(l, player))
                            .collect(Collectors.toList())).create());
        }
    }
}