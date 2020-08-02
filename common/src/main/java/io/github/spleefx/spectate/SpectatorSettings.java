package io.github.spleefx.spectate;

import com.google.gson.annotations.Expose;
import io.github.spleefx.extension.ActionBar;
import io.github.spleefx.extension.ExtensionTitle;
import io.github.spleefx.extension.SlotItemHolder;
import io.github.spleefx.spectate.SpectatePlayerMenu.MenuData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SpectatorSettings {

    @Expose
    public boolean enabled = true;

    @Expose
    private final MenuData spectatePlayerMenu = new MenuData();

    @Expose
    private final ExtensionTitle titleOnSpectate = new ExtensionTitle(true, "&aSpectating {target}", "&8Sneak to exit", 5, 30, 5);

    @Expose
    private final ActionBar spectatingActionBar = new ActionBar(true, "&1Spectating &e{player}");

    @Expose
    private final SlotItemHolder spectateItem = (SlotItemHolder) new SlotItemHolder()
            .setSlot(0)
            .setType("ender_portal_frame")
            .setCount(1)
            .setEnchantments(Collections.emptyMap())
            .setDisplayName("&aSpectate Players")
            .setItemFlags(Collections.emptyList())
            .setLore(Arrays.asList("", "&eClick to spectate other players"));

    @Expose
    private final SlotItemHolder exitSpectatingItem = (SlotItemHolder) new SlotItemHolder()
            .setSlot(8)
            .setType("iron_door")
            .setCount(1)
            .setDisplayName("&cExit Spectating")
            .setEnchantments(Collections.emptyMap())
            .setItemFlags(Collections.emptyList())
            .setLore(Collections.emptyList());

    @Expose
    private final List<PotionEffect> givePotionEffects = new ArrayList<>();

    @Expose
    @Getter
    @Setter
    private boolean sendWarningOnStart = true;

    public MenuData getSpectatePlayerMenu() {
        return spectatePlayerMenu;
    }

    public ExtensionTitle getTitleOnSpectate() {
        return titleOnSpectate;
    }

    public ActionBar getSpectatingActionBar() {
        return spectatingActionBar;
    }

    public SlotItemHolder getSpectateItem() {
        return spectateItem;
    }

    public SlotItemHolder getExitSpectatingItem() {
        return exitSpectatingItem;
    }

    public List<PotionEffect> getGivePotionEffects() {
        return givePotionEffects;
    }
}