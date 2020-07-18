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
package io.github.spleefx.perk;

import com.google.gson.annotations.Expose;
import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.extension.ItemHolder;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.item.ItemFactory;
import io.github.spleefx.util.menu.Button;
import io.github.spleefx.util.menu.GameMenu;
import io.github.spleefx.util.message.message.Message;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.stream.Collectors;

import static io.github.spleefx.data.GameStats.FORMAT;
import static io.github.spleefx.util.game.Chat.colorize;

public class PerkShop {

    @Expose
    private String title;

    @Expose
    private int rows;

    @Expose
    private Map<Integer, PerkItem> items;

    public PerkShop(String title, int rows, Map<Integer, PerkItem> items) {
        this.title = title;
        this.rows = rows;
        this.items = items;
    }

    private static String applyPlaceholders(GamePerk perk, String value) {
        return colorize(value
                .replace("{perk_key}", perk.getKey())
                .replace("{perk_displayname}", perk.getDisplayName())
                .replace("{perk_price}", FORMAT.format(perk.getPurchaseSettings().getPrice()))
                .replace("{perk_usable_amount}", Integer.toString(perk.getPurchaseSettings().getGamesUsableFor())
                        .replace("{perk_ingame_amount}", Integer.toString(perk.getPurchaseSettings().getIngameAmount()))));
    }

    public static class PerkItem extends ItemHolder {

        @Expose
        private String purchasePerk;

        private GamePerk perk;

        public GamePerk getPurchasePerk() {
            return perk == null ? perk = GamePerk.getPerk(purchasePerk) : perk;
        }

        public ItemStack createItem(GamePerk perk) {
            ItemFactory factory = factory();
            ItemStack original = factory.create();
            ItemMeta meta = original.getItemMeta();
            factory.setName(applyPlaceholders(perk, meta.getDisplayName()));
            factory.setLore(meta.getLore().stream().map(s -> applyPlaceholders(perk, s)).collect(Collectors.toList()));
            return factory.create();
        }

    }

    public static class ShopMenu extends GameMenu {

        /**
         * Creates a new menu
         */
        public ShopMenu(PerkShop shop) {
            super(Chat.colorize(shop.title), shop.rows);
            cancelAllClicks = true;
            shop.items.forEach((slot, item) -> setButton(new Button(slot, item.createItem(item.getPurchasePerk()))
                    .addAction(e -> {
                        if (!item.getPurchasePerk().purchase(ArenaPlayer.adapt(((Player) e.getWhoClicked())))) {
                            Message.NOT_ENOUGH_COINS.reply(e.getWhoClicked(), item.getPurchasePerk());
                        }
                    })));
        }
    }
}