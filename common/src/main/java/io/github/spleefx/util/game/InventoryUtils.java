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
package io.github.spleefx.util.game;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A class with utilities for dealing with inventories
 */
public class InventoryUtils {

    /**
     * Removes the items of type from an inventory.
     * <p>
     * Only used in currency exchange.
     *
     * @param inventory Inventory to modify
     * @param type      The type of Material to remove
     * @param amount    The amount to remove, or {@link Integer#MAX_VALUE} to remove all
     */
    public static void removeItems(Inventory inventory, Material type, int amount) {
        if (amount <= 0) return;
        int size = inventory.getSize();
        for (int slot = 0; slot < size; slot++) {
            ItemStack is = inventory.getItem(slot);
            if (is == null) continue;
            if (type == is.getType()) {
                int newAmount = is.getAmount() - amount;
                if (newAmount > 0) {
                    is.setAmount(newAmount);
                    break;
                } else {
                    inventory.clear(slot);
                    amount = -newAmount;
                    if (amount == 0) break;
                }
            }
        }
    }

    /**
     * Removes the items of type from an inventory.
     *
     * @param inventory Inventory to modify
     * @param item      Item to remove
     * @param amount    The amount to remove, or {@link Integer#MAX_VALUE} to remove all
     */
    public static void removeItem(Inventory inventory, ItemStack item, int amount) {
        if (amount <= 0) return;
        int size = inventory.getSize();
        for (int slot = 0; slot < size; slot++) {
            ItemStack is = inventory.getItem(slot);
            if (is == null) continue;
            if (areSimilar(item, is) && is.getAmount() >= amount) {
                int newAmount = is.getAmount() - amount;
                if (newAmount > 0) {
                    is.setAmount(newAmount);
                    break;
                } else {
                    inventory.clear(slot);
                    amount = -newAmount;
                    if (amount == 0) break;
                }
            }
        }
    }

    public static boolean areSimilar(ItemStack first, ItemStack second) {
        if (first == null || second == null) return false;
        if (first.getType() != second.getType()) return false;
        if (first.hasItemMeta() && !second.hasItemMeta() || (second.hasItemMeta() && !first.hasItemMeta()))
            return false;
        if (!first.hasItemMeta() && !second.hasItemMeta() && first.getType() == second.getType()) return true;
        ItemMeta firstMeta = first.getItemMeta();
        ItemMeta secondMeta = second.getItemMeta();
        if (ChatColor.stripColor(firstMeta.getDisplayName()).equals(ChatColor.stripColor(secondMeta.getDisplayName())))
            return firstMeta.hasLore() && secondMeta.hasLore() && firstMeta.getLore().equals(secondMeta.getLore());
        return false;
    }
}
