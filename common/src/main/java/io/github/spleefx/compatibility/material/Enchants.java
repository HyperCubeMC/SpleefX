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

import io.github.spleefx.util.code.MapBuilder;
import io.github.spleefx.util.plugin.Protocol;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Map;

public class Enchants {

    private static final Map<String, Enchantment> ENCHANTMENTS = new HashMap<>();

    static {
        //<editor-fold desc="Enchantments" defaultstate="collapsed">
        //@formatter:off
        MapBuilder<String, Enchantment> m = MapBuilder.of(ENCHANTMENTS)

                .put("0", Enchantment.PROTECTION_ENVIRONMENTAL)
                .put("protection", Enchantment.PROTECTION_ENVIRONMENTAL)

                .put("1", Enchantment.PROTECTION_FIRE)
                .put("protection_fire", Enchantment.PROTECTION_FIRE)
                .put("fire_protection", Enchantment.PROTECTION_FIRE)

                .put("2", Enchantment.PROTECTION_FALL)
                .put("protection_fall", Enchantment.PROTECTION_FALL)
                .put("feather_falling", Enchantment.PROTECTION_FALL)

                .put("3", Enchantment.PROTECTION_EXPLOSIONS)
                .put("protection_explosions", Enchantment.PROTECTION_EXPLOSIONS)
                .put("blast_protection", Enchantment.PROTECTION_EXPLOSIONS)

                .put("4", Enchantment.PROTECTION_PROJECTILE)
                .put("projectile_protection", Enchantment.PROTECTION_PROJECTILE)
                .put("protection_projectile", Enchantment.PROTECTION_PROJECTILE)

                .put("5", Enchantment.OXYGEN)
                .put("oxygen", Enchantment.OXYGEN)
                .put("respiration", Enchantment.OXYGEN)

                .put("6", Enchantment.WATER_WORKER)
                .put("water_worker", Enchantment.WATER_WORKER)
                .put("aqua_affinity", Enchantment.WATER_WORKER)

                .put("7", Enchantment.THORNS)
                .put("thorns", Enchantment.THORNS)

                .put("8", Enchantment.DEPTH_STRIDER)
                .put("depth_strider", Enchantment.DEPTH_STRIDER)

                .put("16", Enchantment.DAMAGE_ALL)
                .put("damage_all", Enchantment.DAMAGE_ALL)
                .put("sharpness", Enchantment.DAMAGE_ALL)

                .put("17", Enchantment.DAMAGE_UNDEAD)
                .put("damage_undead", Enchantment.DAMAGE_UNDEAD)
                .put("smite", Enchantment.DAMAGE_UNDEAD)

                .put("18", Enchantment.DAMAGE_ARTHROPODS)
                .put("damage_arthropods", Enchantment.DAMAGE_ARTHROPODS)
                .put("bane_of_arthropods", Enchantment.DAMAGE_ARTHROPODS)

                .put("19", Enchantment.KNOCKBACK)
                .put("knockback", Enchantment.KNOCKBACK)

                .put("20", Enchantment.FIRE_ASPECT)
                .put("fire_aspect", Enchantment.FIRE_ASPECT)

                .put("21", Enchantment.LOOT_BONUS_MOBS)
                .put("loot_bonus_mobs", Enchantment.LOOT_BONUS_MOBS)
                .put("looting", Enchantment.LOOT_BONUS_MOBS)

                .put("32", Enchantment.DIG_SPEED)
                .put("dig_speed", Enchantment.DIG_SPEED)
                .put("efficiency", Enchantment.DIG_SPEED)

                .put("33", Enchantment.SILK_TOUCH)
                .put("silk_touch", Enchantment.SILK_TOUCH)

                .put("34", Enchantment.DURABILITY)
                .put("durability", Enchantment.DURABILITY)
                .put("unbreaking", Enchantment.DURABILITY)

                .put("35", Enchantment.LOOT_BONUS_BLOCKS)
                .put("fortune", Enchantment.LOOT_BONUS_BLOCKS)
                .put("loot_bonus_blocks", Enchantment.LOOT_BONUS_BLOCKS)

                .put("48", Enchantment.ARROW_DAMAGE)
                .put("arrow_damage", Enchantment.ARROW_DAMAGE)
                .put("power", Enchantment.ARROW_DAMAGE)

                .put("49", Enchantment.ARROW_KNOCKBACK)
                .put("arrow_knockback", Enchantment.ARROW_KNOCKBACK)
                .put("punch", Enchantment.ARROW_KNOCKBACK)

                .put("50", Enchantment.ARROW_FIRE)
                .put("arrow_fire", Enchantment.ARROW_FIRE)
                .put("flame", Enchantment.ARROW_FIRE)

                .put("51", Enchantment.ARROW_INFINITE)
                .put("arrow_infinite", Enchantment.ARROW_INFINITE)
                .put("infinity", Enchantment.ARROW_INFINITE)

                .put("61", Enchantment.LUCK)
                .put("luck", Enchantment.LUCK)
                .put("luck_of_the_sea", Enchantment.LUCK)

                .put("62", Enchantment.LURE)
                .put("lure", Enchantment.LURE)
                .put("luring", Enchantment.LURE);
        if (Protocol.isNewerThan(9))
               m.put("frost_walker", Enchantment.FROST_WALKER)
                .put("mending", Enchantment.MENDING);

        if (Protocol.isNewerThan(11)) {
               m.put("binding_curse", Enchantment.BINDING_CURSE)
                .put("curse_of_binding", Enchantment.BINDING_CURSE)

                .put("vanishing_curse", Enchantment.VANISHING_CURSE)
                .put("curse_of_vanishing", Enchantment.VANISHING_CURSE);
               try {
                   m.put("sweeping_edge", Enchantment.SWEEPING_EDGE);
               } catch (NoSuchFieldError ignored) { // Was added in 1.11.1, would throw if 1.11
               }
        }

        if (Protocol.isNewerThan(13))
            m.put("loyaly", Enchantment.LOYALTY)
             .put("impaling", Enchantment.IMPALING)
             .put("riptide", Enchantment.RIPTIDE)
             .put("channeling", Enchantment.CHANNELING);
/*
        if (Protocol.isNewerThan(14))
            m.put("multishot", Enchantment.MULTISHOT)
             .put("quick_charge", Enchantment.QUICK_CHARGE)
             .put("piercing", Enchantment.PIERCING);*/
        //@formatter:on
        //</editor-fold>
    }

    /**
     * Returns the enchantment from the specified key
     *
     * @param key Key to retrieve from. Can be an ID, a key (such as WATER_WORKER), or a name
     * @return The enchantment, or {@code null} if not found
     */
    public static Enchantment get(String key) {
        return ENCHANTMENTS.get(key.toLowerCase());
    }

}
