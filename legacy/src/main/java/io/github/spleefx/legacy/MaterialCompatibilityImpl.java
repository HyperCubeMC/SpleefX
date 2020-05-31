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
package io.github.spleefx.legacy;

import io.github.spleefx.compatibility.material.MaterialCompatibility;
import io.github.spleefx.util.code.MapBuilder;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class MaterialCompatibilityImpl implements MaterialCompatibility {

    private Map<String, Material> map = MapBuilder.of(new HashMap<String, Material>())
            .put(EXP_BOTTLE, Material.EXP_BOTTLE)
            .put(CLOCK, Material.WATCH)
            .put(REDSTONE_TORCH, Material.REDSTONE_TORCH_ON)
            .put(FIREWORK, Material.FIREWORK)
            .put(DIAMOND_SHOVEL, Material.DIAMOND_SPADE)
            .put(SIGN, Material.SIGN)
            .put(SNOWBALL, Material.SNOW_BALL)
            .put(SUNFLOWER, Material.DOUBLE_PLANT)
            .put(CAULDRON, Material.CAULDRON_ITEM)
            .build();

    /**
     * Returns the appropriate material that represents this wool color
     *
     * @param color Dye color of the wool
     * @return The material
     */
    @Override
    public ItemStack wool(DyeColor color) {
        return new ItemStack(Material.WOOL, 1, color.getWoolData());
    }

    /**
     * Returns the appropriate material that represents this glass pane color
     *
     * @param color Dye color of the pane
     * @return The material
     */
    @Override
    public ItemStack pane(DyeColor color) {
        return new ItemStack(Material.STAINED_GLASS_PANE, 1, color.getWoolData());
    }

    /**
     * A map for accessing the version-appropriate items
     *
     * @return The compatibility map
     */
    @Override
    public Map<String, Material> getCompatibilityMap() {
        return map;
    }

    /**
     * Returns the trident class. Returns {@code Snowball.class} as a fallback for older versions
     *
     * @return The trident class
     */
    @Override
    public Class<? extends Projectile> getTridentClass() {
        return Snowball.class;
    }

    /**
     * Maps the specified material by adding the legacy name or removing it
     *
     * @param original Original material name
     * @return The new material name
     */
    @Override
    public String mapMaterial(String original) {
        return original.toUpperCase()
                .replace("CHEST_MINECART", "STORAGE_MINECART")
                .replace("SNOWBALL", "SNOW_BALL")
                .replace("WOODEN_SHOVEL", "WOOD_SPADE")
                .replace("WOODEN_AXE", "WOOD_AXE")
                .replace("WOODEN_SWORD", "WOOD_SWORD")
                .replace("WOODEN_PICKAXE", "WOOD_PICKAXE")
                .replace("GOLDEN_SHOVEL", "GOLD_SPADE")
                .replace("GOLDEN_AXE", "GOLD_AXE")
                .replace("GOLDEN_SWORD", "GOLD_SWORD")
                .replace("GOLDEN_PICKAXE", "GOLD_PICKAXE")
                .replace("_SHOVEL", "_SPADE");
    }

}