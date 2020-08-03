package io.github.spleefx.data.menu;

import com.google.gson.annotations.Expose;
import io.github.spleefx.config.SpleefXConfig;
import io.github.spleefx.data.PlayerProfile;
import io.github.spleefx.data.PlayerRepository;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.extension.ItemHolder;
import io.github.spleefx.util.PlaceholderUtil;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.item.ItemFactory;
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

    public Inventory asInventory(OfflinePlayer player, GameExtension mode) {
        PlayerProfile stats = PlayerRepository.REPOSITORY.lookup(player);
        Inventory i = Bukkit.createInventory(null, rows * 9, PlaceholderUtil.all(Chat.colorize(title.replace("{extension}", mode != null ? mode.getDisplayName() :
                SpleefXConfig.ALL_MODES_NAME.get())), player, mode));
        for (Entry<Integer, ItemHolder> entry : items.entrySet()) {
            int slot = entry.getKey();
            ItemFactory item = entry.getValue().factory();
            ItemMeta current = item.create().getItemMeta();
            item.setName(PlaceholderUtil.all(current.getDisplayName(), stats, mode));
            if (current.hasLore())
                item.setLore(current.getLore().stream().map(s -> PlaceholderUtil.all(s, stats, mode)).collect(Collectors.toList()));
            i.setItem(slot, item.create());
        }
        return i;
    }
}