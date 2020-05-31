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
import com.google.gson.annotations.SerializedName;
import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.data.GameStats;
import io.github.spleefx.extension.ItemHolder;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.util.item.ItemFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.spleefx.data.GameStats.FORMAT;
import static io.github.spleefx.util.game.Chat.colorize;

@SuppressWarnings("unchecked")
public class SpleggUpgrade {

    @Expose
    private String key;

    @Expose
    private String displayName;

    @Expose
    private double delay;

    @Expose
    @SerializedName("default")
    private boolean isDefault;

    @Expose
    private int price;

    @Expose
    private List<String> requiredUpgradesBefore;

    @Expose
    private GameItem gameItem;

    public SpleggUpgrade(String key, String displayName, double delay, boolean isDefault, int price, List<String> requiredUpgradesBefore, GameItem gameItem) {
        this.key = key;
        this.displayName = displayName;
        this.delay = delay;
        this.isDefault = isDefault;
        this.price = price;
        this.requiredUpgradesBefore = requiredUpgradesBefore;
        this.gameItem = gameItem;
    }

    public String getKey() {
        return key;
    }

    public double getDelay() {
        return delay;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public int getPrice() {
        return price;
    }

    public GameItem getGameItem() {
        return gameItem;
    }

    public boolean purchase(ArenaPlayer player) {
        GameStats stats = player.getStats();
        List<String> purchased = (List<String>) stats.getCustomDataMap().computeIfAbsent("purchasedSpleggUpgrades", (k) -> new ArrayList<String>());
        purchased.addAll(SpleggExtension.EXTENSION.getUpgrades().values().stream().filter(upgrade -> upgrade.isDefault() && !purchased.contains(upgrade.getKey())).map(SpleggUpgrade::getKey).collect(Collectors.toList()));
        if (isDefault || purchased.contains(getKey())) {
            MessageKey.UPGRADE_SELECTED.sendSpleggUpgrade(player.getPlayer(), this);
            stats.getCustomDataMap().put("selectedSpleggUpgrade", getKey());
        } else {
            if (stats.getCoins(player.getPlayer()) >= price) {
                if (purchased.containsAll(requiredUpgradesBefore)) {
                    ((List<String>) stats.getCustomDataMap().computeIfAbsent("purchasedSpleggUpgrades", (k) -> new ArrayList<>()))
                            .add(getKey());
                    stats.getCustomDataMap().put("selectedSpleggUpgrade", getKey());
                    stats.takeCoins(player.getPlayer(), price);
                    MessageKey.UPGRADE_PURCHASED.sendSpleggUpgrade(player.getPlayer(), this);
                } else {
                    MessageKey.MUST_PURCHASE_BEFORE.sendSpleggUpgrade(player.getPlayer(), this);
                }
            } else
                return false;
        }
        return true;
    }

    public static class GameItem extends ItemHolder {

        @Expose
        private int slot;

        public ItemStack createItem(Player player, SpleggUpgrade upgrade) {
            ItemFactory factory = factory();
            ItemStack original = factory.create();
            ItemMeta meta = original.getItemMeta();
            if (meta.hasDisplayName())
                factory.setName(applyPlaceholders(player, upgrade, meta.getDisplayName()));
            if (meta.hasLore())
                factory.setLore(meta.getLore().stream().map(s -> applyPlaceholders(player, upgrade, s)).collect(Collectors.toList()));
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

        public int getSlot() {
            return slot;
        }

        public void setSlot(int slot) {
            this.slot = slot;
        }
    }
}
