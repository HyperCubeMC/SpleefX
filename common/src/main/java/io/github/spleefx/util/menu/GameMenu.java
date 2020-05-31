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
package io.github.spleefx.util.menu;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

import static io.github.spleefx.util.game.Chat.colorize;

/**
 * Represents a GUI menu
 */
public class GameMenu {

    /**
     * A map of all menus
     */
    protected static final Map<String, GameMenu> MENUS = new HashMap<>();

    /**
     * The game
     */
    protected Map<Integer, Button> buttons = new HashMap<>();

    /**
     * An ItemStack that replaces every empty slot
     */
    protected ItemStack scenery;

    /**
     * The inventory title
     */
    protected String title;

    /**
     * Whether should clicks be automatically cancelled or not
     */
    protected boolean cancelAllClicks;

    /**
     * The inventory size
     */
    protected int size;

    /**
     * Creates a new menu
     *
     * @param title Menu title
     * @param rows  Menu rows
     */
    public GameMenu(String title, int rows) {
        Preconditions.checkNotNull(title);
        Preconditions.checkArgument(rows <= 6, "Invalid rows number: " + rows + " (Must be <= 6)");
        this.title = colorize(title);
        size = rows * 9;
        registerToMenus();
    }

    /**
     * Sets the item that is displayed in every empty slot, as a scenery. Clicking this item does nothing.
     *
     * @param scenery Item to fill in
     * @return This menu
     */
    public GameMenu setScenery(ItemStack scenery) {
        this.scenery = scenery;
        return this;
    }

    /**
     * Sets the button
     *
     * @param button Button to add. Set to null to remove.
     * @return The added button
     */
    public GameMenu setButton(Button button) {
        if (button == null) {
            buttons.remove(button.getSlot());
            return this;
        }
        buttons.put(button.getSlot(), button);
        return this;
    }

    /**
     * Returns the button at the specified slot
     *
     * @param slot Slot of the button
     * @return The button, or {@code null} if none
     */
    public Button getButton(int slot) {
        return buttons.get(slot);
    }

    /**
     * Registers this menu to other menus
     *
     * @return This menu.
     */
    public GameMenu registerToMenus() {
        MENUS.put(title, this);
        return this;
    }

    /**
     * Creates the inventory
     *
     * @return The inventory
     */
    public Inventory createInventory() {
        Inventory inventory = Bukkit.createInventory(null, size, title);
        buttons.forEach((slot, button) -> inventory.setItem(slot, button.getItem()));
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null && scenery != null) inventory.setItem(i, scenery);
        }
        return inventory;
    }

    /**
     * Invoked on the inventory click event
     *
     * @param event Event
     */
    protected void onClick(InventoryClickEvent event) {
        if (event.getRawSlot() > event.getInventory().getSize() || event.getSlotType() == InventoryType.SlotType.OUTSIDE)
            return;
        if (!event.getView().getTitle().equals(title)) return;
		if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().isSimilar(scenery)) {
            event.setCancelled(true);
            return;
        }
        if (cancelAllClicks) event.setCancelled(true);
        Button button = getButton(event.getRawSlot());
        if (button != null && button.getOnClick() != null) button.getOnClick().forEach(task -> task.accept(event));
    }

    public static class MenuListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void onInventoryClick(InventoryClickEvent event) {
            GameMenu menu = MENUS.get(event.getView().getTitle());
            if (menu == null) return;
            menu.onClick(event);
        }
    }
}