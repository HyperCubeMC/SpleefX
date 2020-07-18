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
package io.github.spleefx.gui;

import com.google.gson.annotations.Expose;
import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.ArenaStage;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.extension.ItemHolder;
import io.github.spleefx.util.PlaceholderUtil;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.item.ItemFactory;
import io.github.spleefx.util.menu.Button;
import io.github.spleefx.util.menu.GameMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.moltenjson.configuration.select.SelectKey;
import org.moltenjson.configuration.select.SelectionHolder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static io.github.spleefx.data.GameStats.FORMAT;

public class JoinGUI extends GameMenu {

    /**
     * Creates a new menu
     */
    public JoinGUI(MenuSettings menu, Player player, GameExtension extension) {
        super(Chat.colorize(menu.title).replace("{player}", player.getName()).replace("{extension}", extension != null ? extension.getDisplayName() : ""), menu.rows);
        AtomicInteger slot = new AtomicInteger();
        cancelAllClicks = true;
        GameArena.ARENAS.get().values()
                .stream()
                .filter(a -> a.getExtension().getKey().equals(extension.getKey()))
                .filter(a -> menu.stagesToDisplay.contains(a.getEngine().getArenaStage()))
                .forEach(a -> createButton(menu, a, slot));
        display(player);
    }

    private void createButton(MenuSettings menu, GameArena arena, AtomicInteger slot) {
        setButton(new Button(slot.getAndIncrement(),
                MenuSettings.applyPlaceholders(menu.items.get(arena.getEngine().getArenaStage()).factory(), arena))
                .addAction(e -> e.getWhoClicked().closeInventory())
                .addAction(e -> arena.getEngine().join(ArenaPlayer.adapt(((Player) e.getWhoClicked())), null)));
    }

    public static class MenuSettings {

        @SelectKey("menu")
        public static final SelectionHolder<MenuSettings> MENU = new SelectionHolder<>(null);

        /**
         * The menu title
         */
        @Expose
        private String title;

        /**
         * The rows
         */
        @Expose
        private int rows;

        /**
         * The stages that are displayed in the gui
         */
        @Expose
        private List<ArenaStage> stagesToDisplay;

        /**
         * The menu items
         */
        @Expose
        private Map<ArenaStage, ItemHolder> items;

        private static ItemStack applyPlaceholders(ItemFactory item, GameArena arena) {
            ItemMeta current = item.create().getItemMeta();
            item.setName(placeholders(current.getDisplayName(), arena));
            if (current.hasLore())
                item.setLore(current.getLore().stream().map(s -> placeholders(s, arena)).collect(Collectors.toList()));
            return item.create();
        }

        private static String placeholders(String string, GameArena arena) {
            return PlaceholderUtil.all(string, arena);
        }

    }

}
