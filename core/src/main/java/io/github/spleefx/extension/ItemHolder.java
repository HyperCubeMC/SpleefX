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
package io.github.spleefx.extension;

import com.google.gson.annotations.Expose;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.util.item.ItemFactory;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.github.spleefx.util.plugin.Protocol.PROTOCOL;

/**
 * A class which holds data for an {@link org.bukkit.inventory.ItemStack}
 */
public class ItemHolder {

    @Expose
    private String type;

    @Expose
    private int count = 1;

    @Expose
    private Map<Enchantment, Integer> enchantments;

    @Expose
    private String displayName = "{}";

    @Expose
    private List<String> lore = Collections.emptyList();

    @Expose
    private List<ItemFlag> itemFlags = Collections.emptyList();

    @Expose
    private String color = null;

    @Expose
    private boolean unbreakable = false;

    public ItemFactory factory() {
        type = CompatibilityHandler.getMaterialCompatibility().mapMaterial(type);
        ItemStack item;
        DyeColor color = this.color == null ? null : DyeColor.valueOf(this.color.toUpperCase());
        if (color != null)
            if (PROTOCOL >= 13) // The flattening, in 1.13+
                item = new ItemStack(Material.matchMaterial(color + "_" + type.toUpperCase()));
            else
                item = new ItemStack(Material.matchMaterial(type.toUpperCase()), 1, color.getWoolData());
        else
            item = new ItemStack(Material.matchMaterial(type.toUpperCase()));
        ItemFactory f = ItemFactory.create(item).setAmount(count).addEnchantments(enchantments)
                .setLore(lore).addItemFlags(itemFlags).setUnbreakable(unbreakable);
        if (!displayName.equals("{}")) f.setName(displayName);
        return f;
    }

    public ItemHolder setType(String type) {
        this.type = type;
        return this;
    }

    public ItemHolder setCount(int count) {
        this.count = count;
        return this;
    }

    public ItemHolder setEnchantments(Map<Enchantment, Integer> enchantments) {
        this.enchantments = enchantments;
        return this;
    }

    public ItemHolder setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public ItemHolder setLore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    public ItemHolder setItemFlags(List<ItemFlag> itemFlags) {
        this.itemFlags = itemFlags;
        return this;
    }

    public ItemHolder setColor(String color) {
        this.color = color;
        return this;
    }

    public ItemHolder setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }
}