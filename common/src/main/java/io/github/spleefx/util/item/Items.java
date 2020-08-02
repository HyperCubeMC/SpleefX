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
package io.github.spleefx.util.item;

import io.github.spleefx.compatibility.material.MaterialCompatibility;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import static io.github.spleefx.compatibility.CompatibilityHandler.getMaterialCompatibility;
import static io.github.spleefx.compatibility.material.MaterialCompatibility.CAULDRON;

/**
 * A class with constants of items
 */
public class Items {

    /* Page index controlling items*/

    public static final ItemStack PREVIOUS_PAGE = ItemFactory.create(Material.ARROW)
            .setName("&dPrevious Page").create();

    public static final ItemStack NEXT_PAGE = ItemFactory.create(Material.ARROW)
            .setName("&dNext Page").create();

    /* Core items */

    public static final ItemStack BARRIER = ItemFactory.create(Material.BARRIER)
            .setName("&eNo teams in this arena!")
            .create();

    public static final ItemStack RED_TEAM = ItemFactory.create(getMaterialCompatibility().wool(DyeColor.RED))
            .setName("&cRed Team")
            .create();

    public static final ItemStack GREEN_TEAM = ItemFactory.create(getMaterialCompatibility().wool(DyeColor.LIME))
            .setName("&aGreen Team")
            .create();

    public static final ItemStack BLUE_TEAM = ItemFactory.create(getMaterialCompatibility().wool(DyeColor.BLUE))
            .setName("&9Blue Team")
            .create();

    public static final ItemStack YELLOW_TEAM = ItemFactory.create(getMaterialCompatibility().wool(DyeColor.YELLOW))
            .setName("&eYellow Team")
            .create();

    public static final ItemStack PINK_TEAM = ItemFactory.create(getMaterialCompatibility().wool(DyeColor.PINK))
            .setName("&dPink Team")
            .create();

    public static final ItemStack GRAY_TEAM = ItemFactory.create(getMaterialCompatibility().wool(DyeColor.GRAY))
            .setName("&8Gray Team")
            .create();

    public static final ItemStack RENAME_ARENA = ItemFactory.create(Material.NAME_TAG)
            .setName("&eRename Arena")
            .create();

    public static final ItemStack DEATH_LEVEL = ItemFactory.create(Material.REDSTONE)
            .setName("&cDeath Level")
            .setLore("&aThe Y value in which if players reach, they lose")
            .create();

    public static final ItemStack MEMBERS_PER_TEAM = ItemFactory.create(Material.IRON_HELMET)
            .setName("&eMembers per team")
            .setLore("&aHow many people in each team")
            .create();

    public static final ItemStack MAX_COUNT = ItemFactory.create(Material.IRON_HELMET)
            .setName("&eMaximum player count")
            .setLore("&aThe maximum amount of players in the arena")
            .create();

    public static final ItemStack GAME_TIME = ItemFactory.create(getMaterialCompatibility().get(MaterialCompatibility.CLOCK))
            .setName("&eGame time")
            .setLore("&aHow many minutes the game can last for")
            .create();

    public static final ItemStack MINIMUM = ItemFactory.create(Material.HOPPER)
            .setName("&eMinimum players required")
            .setLore("&aMinimum amount of players required for the game to start")
            .create();

    public static final ItemStack BET_INT = ItemFactory.create(getMaterialCompatibility().get(MaterialCompatibility.SUNFLOWER))
            .setName("&eBet amount")
            .setLore("&aThe amount which players have to pay to join, and ", "&athe sum of bets is given to the winner")
            .create();

    public static final ItemStack BET_BOOL = ItemFactory.create(getMaterialCompatibility().get(CAULDRON))
            .setName("&eTake bets")
            .setLore("&aWhether should players be required to pay a bet ", "&a(which is given to the winner) to join", "&athe game")
            .create();

    public static final ItemStack MELTING = ItemFactory.create(Material.LAVA_BUCKET)
            .setName("&eMelting")
            .setLore("&aShould snow melt around players when they are not moving")
            .create();

    public static final ItemStack DESTROYABLE = ItemFactory.create(Material.ANVIL)
            .setName("&eAll blocks destroyable by default")
            .setLore("&aWhether are all blocks destroyable by default.", "", "&7If &aenabled&7, blocks in /splegg materials will be ",
                    "&enon-destroyable&7.", "", "&7If &cdisabled&7, blocks in /splegg materials will be", "&edestroyable", "", "&a/splegg materials")
            .create();

    public static final ItemStack DROP_MINED_BLOCKS = ItemFactory.create(Material.BRICK)
            .setName("&eDrop mined blocks")
            .setLore("&aShould mined blocks have their standard drops dropped")
            .create();

    public static final ItemStack POWERUPS = ItemFactory.create(getMaterialCompatibility().get(MaterialCompatibility.FIREWORK))
            .setName("&ePower Ups")
            .setLore("&aShould power ups be dropped in the game or not")
            .create();

    public static final ItemStack DELETE = ItemFactory.create(Material.TNT)
            .setName("&cDelete arena")
            .setLore("&aDelete the arena and all its data_old")
            .create();

    /* Boolean-controlling items */

    public static final ItemStack ADD_TEAM = ItemFactory.create(Material.LEVER)
            .setName("&aAdd Team")
            .create();

    public static final ItemStack REMOVE_TEAM = ItemFactory.create(getMaterialCompatibility().get(MaterialCompatibility.REDSTONE_TORCH))
            .setName("&cRemove Team")
            .create();

    public static final ItemStack ENABLE = ItemFactory.create(Material.LEVER)
            .setName("&aEnable")
            .create();

    public static final ItemStack DISABLE = ItemFactory.create(getMaterialCompatibility().get(MaterialCompatibility.REDSTONE_TORCH))
            .setName("&cDisable")
            .create();

    /* Integer-controlling items */

    public static final ItemFactory INCREASE = ItemFactory.create(getMaterialCompatibility().pane(DyeColor.LIME))
            .setName("&aIncrease");

    public static final ItemFactory DECREASE = ItemFactory.create(getMaterialCompatibility().pane(DyeColor.RED))
            .setName("&cDecrease");

}