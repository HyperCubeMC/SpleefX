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

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a button
 */
public class Button {

    public static final Consumer<InventoryClickEvent> CANCEL_ACTION = event -> event.setCancelled(true);

    public static final Consumer<InventoryClickEvent> CLOSE_INVENTORY = (event) -> event.getWhoClicked().closeInventory();

    /**
     * The item representing this button
     */
    private ItemStack item;

    /**
     * The button slot
     */
    private int slot;

    /**
     * The click task
     */
    private List<Consumer<InventoryClickEvent>> onClick = new ArrayList<>();

    public Button(int slot, ItemStack item) {
        this.slot = slot;
        this.item = item;
    }

    public ItemStack getItem() {
        return item;
    }

    public int getSlot() {
        return slot;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public Button addAction(Consumer<InventoryClickEvent> action) {
        onClick.add(action);
        return this;
    }

    public List<Consumer<InventoryClickEvent>> getOnClick() {
        return onClick;
    }

    public static Consumer<InventoryClickEvent> createRedirect(GameMenu menu) {
        if (menu == null) return CLOSE_INVENTORY;
        return event -> event.getWhoClicked().openInventory(menu.createInventory());
    }

}