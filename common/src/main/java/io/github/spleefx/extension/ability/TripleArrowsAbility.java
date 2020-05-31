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
package io.github.spleefx.extension.ability;

import com.google.gson.annotations.Expose;
import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.ModeType;
import io.github.spleefx.arena.bow.BowSpleefArena;
import io.github.spleefx.compatibility.material.Enchants;
import io.github.spleefx.util.game.Metas;
import io.github.spleefx.util.plugin.DelayExecutor;
import io.github.spleefx.util.plugin.DelayExecutor.DelayData;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.*;

import static io.github.spleefx.arena.bow.BowSpleefEngine.ARROW_METADATA;
import static io.github.spleefx.compatibility.CompatibilityHandler.either;
import static io.github.spleefx.extension.standard.bowspleef.BowSpleefExtension.EXTENSION;

public class TripleArrowsAbility implements Listener {

    /**
     * The delay handler
     */
    private DelayExecutor<GameAbility> delayExecutor;

    public TripleArrowsAbility(DelayExecutor<GameAbility> delayExecutor) {
        this.delayExecutor = delayExecutor;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (EXTENSION.getTripleArrows().actionsToTrigger == null || !EXTENSION.getTripleArrows().actionsToTrigger.contains(event.getAction()))
            return;
        ArenaPlayer player = ArenaPlayer.adapt(event.getPlayer());
        if (player.getCurrentArena() == null || player.getCurrentArena().type != ModeType.BOW_SPLEEF) return;
        BowSpleefArena arena = player.getCurrentArena();
        Settings settings = EXTENSION.getTripleArrows();
        if (settings.getRequiredMaterials().contains(either(() -> event.getPlayer().getInventory().getItemInMainHand(), () -> event.getPlayer().getItemInHand()).getType()))
            launchTripleArrowsIfPossible(player.getPlayer(), arena);
    }

    private void launchTripleArrowsIfPossible(Player player, BowSpleefArena arena) {
        if (!EXTENSION.getTripleArrows().isEnabled()) return;
        if (delayExecutor.hasDelay(player, GameAbility.TRIPLE_ARROWS)) return;
        if (arena.getEngine().getAbilityCount().get(player.getUniqueId()).getOrDefault(GameAbility.TRIPLE_ARROWS, 0) <= 0)
            return; // Player has no more double jumps
        ItemStack mainHand = either(() -> player.getInventory().getItemInMainHand(), () -> player.getItemInHand());
        boolean flame = mainHand.getType().name().contains("BOW") &&
                mainHand.getItemMeta().hasEnchant(Enchants.get("flame"));
        FixedMetadataValue v = new FixedMetadataValue(SpleefX.getPlugin(), arena);
        Arrow first = player.launchProjectile(Arrow.class);
        Metas.set(first, ARROW_METADATA, v);
        Vector o = first.getVelocity();
        Arrow second = player.launchProjectile(Arrow.class, new Vector().setX(o.getX()).setY(o.getY()).setZ(o.getZ() - 1.5));
        Metas.set(second, ARROW_METADATA, v);
        Arrow third = player.launchProjectile(Arrow.class, new Vector().setX(o.getX() + 1.5).setY(o.getY()).setZ(o.getZ()));
        Metas.set(third, ARROW_METADATA, v);
        if (flame) {
            first.setFireTicks(Integer.MAX_VALUE);
            second.setFireTicks(Integer.MAX_VALUE);
            third.setFireTicks(Integer.MAX_VALUE);
        }
        delayExecutor.setDelay(player, GameAbility.TRIPLE_ARROWS, new DelayData(EXTENSION.getTripleArrows().getCooldown()));
        GameAbility.TRIPLE_ARROWS.reduceAbility(arena.getEngine().getAbilityCount().get(player.getUniqueId()));
    }

    public static class Settings {

        private static final Set<Material> MATERIALS = new HashSet<>();

        private static final List<Action> CLICKS = Arrays.asList(Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK);

        @Expose
        private boolean enabled = true;

        @Expose
        private int defaultAmount = 5;

        @Expose
        private Set<Action> actionsToTrigger = new HashSet<>(CLICKS);

        @Expose
        private List<Material> requiredMaterials = new ArrayList<>(MATERIALS);

        @Expose
        private int cooldown = 3;

        public boolean isEnabled() {
            return enabled;
        }

        public int getCooldown() {
            return cooldown;
        }

        public List<Material> getRequiredMaterials() {
            return requiredMaterials;
        }

        public int getDefaultAmount() {
            return defaultAmount;
        }

        static {
            MATERIALS.add(Material.BOW);
            Material cross = Material.matchMaterial("CROSSBOW");
            if (cross != null) MATERIALS.add(cross);
        }
    }
}
