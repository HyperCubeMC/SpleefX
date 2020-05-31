package io.github.spleefx.data;

import com.google.gson.annotations.Expose;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.extension.ItemHolder;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.item.ItemFactory;
import io.github.spleefx.util.plugin.PluginSettings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Represents a menu
 */
public class StatsMenu {

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
     * The menu items
     */
    @Expose
    private Map<Integer, ItemHolder> items;

    public Inventory asInventory(OfflinePlayer player, GameStats stats, GameExtension mode) {
        Inventory i = Bukkit.createInventory(null, rows * 9, Chat.colorize(title.replace("{player}", player.getName()).replace("{extension}", mode != null ? mode.getDisplayName() : PluginSettings.ALL_MODES_NAME.get())));
        for (Entry<Integer, ItemHolder> entry : items.entrySet()) {
            int slot = entry.getKey();
            ItemFactory item = entry.getValue().factory();
            ItemMeta current = item.create().getItemMeta();
            item.setName(applyPlaceholders(current.getDisplayName(), stats, mode));
            if (current.hasLore())
                item.setLore(current.getLore().stream().map(s -> applyPlaceholders(s, stats, mode)).collect(Collectors.toList()));
            i.setItem(slot, item.create());
        }
        return i;
    }

    private static String applyPlaceholders(String string, GameStats stats, GameExtension mode) {
        return string
                .replace("{games_played}", Integer.toString(stats.get(PlayerStatistic.GAMES_PLAYED, mode)))
                .replace("{wins}", Integer.toString(stats.get(PlayerStatistic.WINS, mode)))
                .replace("{losses}", Integer.toString(stats.get(PlayerStatistic.LOSSES, mode)))
                .replace("{draws}", Integer.toString(stats.get(PlayerStatistic.DRAWS, mode)))
                .replace("{blocks_mined}", Integer.toString(stats.get(PlayerStatistic.BLOCKS_MINED, mode)));
    }

}