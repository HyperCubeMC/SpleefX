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
package io.github.spleefx.compatibility.material;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * An interface for abstracting materials across versions
 */
public interface MaterialCompatibility {

    String EXP_BOTTLE = "exp_bottle";
    String CLOCK = "clock";
    String REDSTONE_TORCH = "redstone_torch";
    String FIREWORK = "firework";
    String DIAMOND_SHOVEL = "dshovel";
    String SIGN = "sign";
    String SNOWBALL = "snowball";
    String SUNFLOWER = "sunflower";
    String CAULDRON = "cauldron";

    /**
     * Returns the appropriate material that represents this wool color
     *
     * @param color Dye color of the wool
     * @return The material
     */
    ItemStack wool(DyeColor color);

    /**
     * Returns the appropriate material that represents this glass pane color
     *
     * @param color Dye color of the pane
     * @return The material
     */
    ItemStack pane(DyeColor color);

    /**
     * A map for accessing the version-appropriate items
     *
     * @return The compatibility map
     */
    Map<String, Material> getCompatibilityMap();

    /**
     * Returns the trident class. Returns {@code Snowball.class} as a fallback for older versions
     *
     * @return The trident class
     */
    Class<? extends Projectile> getTridentClass();

    /**
     * Maps the specified material by adding the legacy name or removing it
     *
     * @param original Original material name
     * @return The new material name
     */
    String mapMaterial(String original);

    /**
     * Returns the material assigned to the key
     *
     * @param key Key of the material. Fetched from constants in {@link MaterialCompatibility}.
     * @return The material
     */
    default Material get(String key) {
        return getCompatibilityMap().get(key);
    }

}
