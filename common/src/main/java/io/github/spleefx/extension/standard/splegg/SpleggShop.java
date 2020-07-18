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
package io.github.spleefx.extension.standard.splegg;

import com.google.gson.annotations.Expose;
import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.data.GameStats;
import io.github.spleefx.extension.ItemHolder;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.item.ItemFactory;
import io.github.spleefx.util.menu.Button;
import io.github.spleefx.util.menu.GameMenu;
import io.github.spleefx.util.message.message.Message;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.spleefx.data.GameStats.FORMAT;
import static io.github.spleefx.util.game.Chat.colorize;

public class SpleggShop {

    @Expose
    private String title;

    @Expose
    private int rows;

    @Expose
    private Map<Integer, SpleggShopItem> items;

    public SpleggShop(String title, int rows, Map<Integer, SpleggShopItem> items) {
        this.title = title;
        this.rows = rows;
        this.items = items;
    }

    public static class SpleggShopItem extends ItemHolder {

        @Expose
        private String purchaseUpgrade;

        private SpleggUpgrade upgrade;

        public SpleggUpgrade getUpgrade() {
            return upgrade == null ? upgrade = SpleggExtension.EXTENSION.getUpgrades().get(purchaseUpgrade) : upgrade;
        }

        public ItemStack createItem(Player player) {
            ItemFactory factory = factory();
            ItemStack original = factory.create();
            ItemMeta meta = original.getItemMeta();
            factory.setName(applyPlaceholders(player, getUpgrade(), meta.getDisplayName()));
            factory.setLore(meta.getLore().stream().map(s -> applyPlaceholders(player, getUpgrade(), s)).collect(Collectors.toList()));
            return factory.create();
        }

        private static String applyPlaceholders(Player player, SpleggUpgrade upgrade, String value) {
            GameStats stats = ArenaPlayer.adapt(player).getStats();
            return colorize(value
                    .replace("{upgrade_key}", upgrade.getKey())
                    .replace("{upgrade_price}", FORMAT.format(upgrade.getPrice()))
                    .replace("{upgrade_delay}", Double.toString(upgrade.getDelay())))
                    .replace("{upgrade_purchased}", ((List<String>) stats.getCustomDataMap().computeIfAbsent("purchasedSpleggUpgrades", (k) -> new ArrayList<String>())).contains(upgrade.getKey()) ? "&aClick to select" : (stats.getCoins(player) >= upgrade.getPrice() ? "&aClick to purchase" : "&cYou don't have enough coins"));
        }

    }

    public static class SpleggMenu extends GameMenu {

        /**
         * Creates a new menu
         *
         * @param shop The shop instance
         */
        public SpleggMenu(SpleggShop shop, Player p) {
            super(Chat.colorize(shop.title), shop.rows);
            cancelAllClicks = true;
            shop.items.forEach((slot, item) -> setButton(new Button(slot, item.createItem(p)).addAction(e -> {
                ArenaPlayer player = ArenaPlayer.adapt((Player) e.getWhoClicked());
                if (!item.getUpgrade().purchase(player))
                    Message.NOT_ENOUGH_COINS_SPLEGG.reply(player.getPlayer(), item.getUpgrade());
            })));
        }
    }
}