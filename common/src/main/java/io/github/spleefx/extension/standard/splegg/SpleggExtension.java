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
package io.github.spleefx.extension.standard.splegg;

import com.google.gson.annotations.Expose;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.extension.ItemHolder;
import io.github.spleefx.util.game.ExplosionSettings;
import io.github.spleefx.util.plugin.Protocol;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpleggExtension extends GameExtension {

    public static final SpleggExtension EXTENSION =
            ExtensionsManager.getExtension("splegg", ExtensionType.STANDARD, SpleggExtension.class);

    @Expose
    private ItemHolder projectileItem;

    @Expose
    private int projectileItemSlot;

    @Expose
    private ProjectileType projectileType;

    @Expose
    private boolean upgradeSystemEnabled;

    @Expose
    private ExplosionSettings explodeTNTWhenHit;

    @Expose
    private List<Material> nonDestroyableBlocks;

    @Expose
    private List<Action> clickActions;

    @Expose
    private Map<String, SpleggUpgrade> upgrades = new HashMap<>();

    @Expose
    private SpleggShop spleggShop;

    public ItemHolder getProjectileItem() {
        return projectileItem;
    }

    public ProjectileType getProjectileType() {
        return projectileType;
    }

    public ExplosionSettings getExplodeTNTWhenHit() {
        return explodeTNTWhenHit;
    }

    public int getProjectileItemSlot() {
        return projectileItemSlot;
    }

    public boolean isDestroyable(Material material) {
        return !nonDestroyableBlocks.contains(material);
    }

    public boolean isUpgradeSystemEnabled() {
        return upgradeSystemEnabled;
    }

    public List<Action> getClickActions() {
        return clickActions;
    }

    public SpleggShop getSpleggShop() {
        return spleggShop;
    }

    public Map<String, SpleggUpgrade> getUpgrades() {
        return upgrades;
    }

    /**
     * Represents different projectile types
     */
    public enum ProjectileType {

        /**
         * Represents an arrow projectile
         */
        ARROW(-1, Arrow.class),

        /**
         * Represents a snowball projectile
         */
        SNOWBALL(-1, Snowball.class),

        /**
         * Represents an egg projectile
         */
        EGG(-1, Egg.class),

        /**
         * Represents a fireball projectile
         */
        FIREBALL(-1, Fireball.class),

        /**
         * Represents a trident projectile
         */
        TRIDENT(14, CompatibilityHandler.getMaterialCompatibility().getTridentClass());

        /**
         * The projectile class
         */
        private Class<? extends Projectile> clazz;

        ProjectileType(int protocol, Class<? extends Projectile> clazz) {
            if (protocol == -1) {
                this.clazz = clazz;
                return;
            }
            if (Protocol.isNewerThan(protocol))
                this.clazz = clazz;
            else
                this.clazz = Snowball.class; // fallback
        }

        public Class<? extends Projectile> getProjectileClass() {
            return clazz;
        }
    }

}