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
import io.github.spleefx.data.PlayerProfile;
import io.github.spleefx.extension.ItemHolder;
import io.github.spleefx.util.PlaceholderUtil;
import io.github.spleefx.util.item.ItemFactory;
import io.github.spleefx.util.message.message.Message;
import lombok.ToString;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.spleefx.util.game.Chat.colorize;

@ToString
public class SpleggUpgrade {

    @Expose
    private final String key;

    @Expose
    private final String displayName;

    @Expose
    private final double delay;

    @Expose
    @SerializedName("default")
    private final boolean isDefault;

    @Expose
    private final int price;

    @Expose
    private final List<String> requiredUpgradesBefore;

    @Expose
    private final GameItem gameItem;

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
        PlayerProfile profile = player.getStats(); // these are immutable soo
        PlayerProfile.Builder stats = profile.asBuilder();
        Set<String> upgrades = profile.upgradeKeys();

        // add default upgrades
        SpleggExtension.EXTENSION.getUpgrades().values().stream()
                .filter(upgrade -> upgrade.isDefault() && !upgrades.contains(upgrade.getKey()))
                .forEach(stats::addPurchase);

        if (isDefault || profile.getPurchasedSpleggUpgrades().contains(this)) {
            Message.UPGRADE_SELECTED.reply(player.getPlayer(), this);
            stats.setSpleggUpgrade(getKey());
        } else {
            if (profile.getCoins() >= price) {
                if (profile.upgradeKeys().containsAll(requiredUpgradesBefore)) {
                    stats.addPurchase(this);
                    stats.setSpleggUpgrade(getKey());
                    stats.subtractCoins(price);
                    Message.UPGRADE_PURCHASED.reply(player.getPlayer(), this);
                } else {
                    Message.MUST_PURCHASE_BEFORE.reply(player.getPlayer(), this);
                }
            } else
                return false;
        }
        stats.push();
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
            PlayerProfile stats = ArenaPlayer.adapt(player).getStats();
            return colorize(value
                    .replace("{upgrade_key}", upgrade.getKey())
                    .replace("{upgrade_price}", PlaceholderUtil.NUMBER_FORMAT.format(upgrade.getPrice()))
                    .replace("{upgrade_delay}", Double.toString(upgrade.getDelay())))
                    .replace("{upgrade_purchased}", stats.upgradeKeys().contains(upgrade.getKey()) ?
                            "&aClick to select" : (stats.getCoins() >= upgrade.getPrice() ?
                            "&aClick to purchase" : "&cYou don't have enough coins"));
        }

        public int getSlot() {
            return slot;
        }

        public void setSlot(int slot) {
            this.slot = slot;
        }
    }
}
