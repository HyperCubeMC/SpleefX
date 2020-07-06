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
package io.github.spleefx.modern;

import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.compatibility.material.MaterialCompatibility;
import io.github.spleefx.util.code.MapBuilder;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class MaterialCompatibilityImpl implements MaterialCompatibility {

    private Map<String, Material> map = MapBuilder.of(new HashMap<String, Material>())
            .put(EXP_BOTTLE, Material.EXPERIENCE_BOTTLE)
            .put(CLOCK, Material.CLOCK)
            .put(REDSTONE_TORCH, Material.REDSTONE_TORCH)
            .put(FIREWORK, Material.FIREWORK_ROCKET)
            .put(DIAMOND_SHOVEL, Material.DIAMOND_SHOVEL)
            .put(SIGN, CompatibilityHandler.either(() -> Material.valueOf("SIGN"), () -> Material.valueOf("OAK_SIGN")))
            .put(SNOWBALL, Material.SNOWBALL)
            .put(SUNFLOWER, Material.SUNFLOWER)
            .put(CAULDRON, Material.CAULDRON)
            .put(END_FRAME, Material.END_PORTAL_FRAME)
            .build();

    /**
     * Returns the appropriate material that represents this wool color
     *
     * @param color Dye color of the wool
     * @return The material
     */
    @Override
    public ItemStack wool(DyeColor color) {
        return new ItemStack(Objects.requireNonNull(Material.matchMaterial(color.name() + "_WOOL")));
    }

    /**
     * Creates a skull of the specified player
     *
     * @param owner Owner of the skull
     * @return The ItemStack representing this skull
     */
    @Override
    public CompletableFuture<ItemStack> skull(OfflinePlayer owner) {
        CompletableFuture<ItemStack> future = new CompletableFuture<>();
        HEAD.submit(() -> {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(owner);
            skull.setItemMeta(meta);
            future.complete(skull);
        });
        return future;
    }

    /**
     * Returns the appropriate material that represents this glass pane color
     *
     * @param color Dye color of the pane
     * @return The material
     */
    @Override
    public ItemStack pane(DyeColor color) {
        return new ItemStack(Objects.requireNonNull(Material.matchMaterial(color.name() + "_STAINED_GLASS_PANE")));
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
        return Trident.class;
    }

    /**
     * Maps the specified material by adding the legacy name or removing it
     *
     * @param original Original material name
     * @return The new material name
     */
    @Override
    public String mapMaterial(String original) {
        return original.toUpperCase().replace("_SPADE", "_SHOVEL")
                .replace("STORAGE_MINECART", "CHEST_MINECART")
                .replace("SNOW_BALL", "SNOWBALL")
                .replace("WOOD_SHOVEL", "WOODEN_SHOVEL")
                .replace("WOOD_AXE", "WOODEN_AXE")
                .replace("WOOD_SWORD", "WOODEN_SWORD")
                .replace("WOOD_PICKAXE", "WOODEN_PICKAXE")
                .replace("GOLD_SHOVEL", "GOLDEN_SHOVEL")
                .replace("GOLD_AXE", "GOLDEN_AXE")
                .replace("GOLD_SWORD", "GOLDEN_SWORD")
                .replace("ENDER_PORTAL_FRAME", "END_PORTAL_FRAME")
                .replace("GOLD_PICKAXE", "GOLDEN_PICKAXE");
    }

}
