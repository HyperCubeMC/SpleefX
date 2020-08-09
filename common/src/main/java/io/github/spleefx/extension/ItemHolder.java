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
import com.google.gson.annotations.JsonAdapter;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.util.item.ItemFactory;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.moltenjson.adapter.EnchantmentsAdapter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.github.spleefx.util.plugin.Protocol.PROTOCOL;

/**
 * A class which holds data for an {@link ItemStack}
 */
@Setter
@Accessors(chain = true)
public class ItemHolder {

    @Expose protected String type;

    @Expose
    protected int count = 1;

    @Expose
    @JsonAdapter(EnchantmentsAdapter.class)
    protected Map<Enchantment, Integer> enchantments;

    @Expose
    protected String displayName = "{}";

    @Expose
    protected List<String> lore = Collections.emptyList();

    @Expose
    protected List<ItemFlag> itemFlags = Collections.emptyList();

    @Expose
    protected String color = null;

    @Expose
    protected boolean unbreakable = false;

    public ItemFactory factory() {
        type = CompatibilityHandler.getMaterialCompatibility().mapMaterial(type);
        ItemStack item;
        DyeColor color = this.color == null ? null : DyeColor.valueOf(this.color.toUpperCase());
        if (color != null)
            item = new ItemStack(Objects.requireNonNull(Material.matchMaterial(color + "_" + type.toUpperCase())));
        else
            item = new ItemStack(Objects.requireNonNull(Material.matchMaterial(type.toUpperCase())));
        ItemFactory f = ItemFactory.create(item).setAmount(count).addEnchantments(enchantments)
                .setLore(lore).addItemFlags(itemFlags).setUnbreakable(unbreakable);
        if (!displayName.equals("{}")) f.setName(displayName);
        return f;
    }

}