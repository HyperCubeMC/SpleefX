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
package io.github.spleefx.perk;

import com.google.gson.annotations.Expose;
import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.ArenaStage;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.extension.ItemHolder;
import io.github.spleefx.extension.standard.splegg.SpleggExtension.ProjectileType;
import io.github.spleefx.util.game.Metas;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

/**
 * A snowball that removes blocks it gets shoot in
 */
@SuppressWarnings("Convert2MethodRef")
public class AcidicSnowballsPerk extends GamePerk implements Listener {

    private static final String METADATA = "acidic_snowball";

    @Expose
    private ProjectileType projectileType;

    @Expose
    private int projectileItemSlot;

    @Expose
    private ItemHolder projectileItem;

    @Expose
    private Set<Material> nonDestroyableBlocks = new HashSet<>();

    @Expose
    private Set<Action> clickTypes = new HashSet<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!e.hasItem()) return;
        Player p = e.getPlayer();
        ArenaPlayer player = ArenaPlayer.adapt(e.getPlayer());

        if (clickTypes == null || !clickTypes.contains(e.getAction())) return;
        GameArena arena = player.getCurrentArena();
        if (arena == null) return;
        if (arena.getEngine().getArenaStage() != ArenaStage.ACTIVE) return;
        if (!canUse(player.getCurrentArena().getExtension())) return;
        if (!CompatibilityHandler.either(() -> p.getInventory().getItemInMainHand(), () -> p.getItemInHand()).isSimilar(projectileItem.factory().create())) return;
        onActivate(player);
        e.setCancelled(true);
    }

    /**
     * Gives this perk to the specified player
     *
     * @param player Player to give
     */
    @Override
    public void giveToPlayer(ArenaPlayer player) {
        super.giveToPlayer(player);
        player.getPlayer().getInventory().setItem(projectileItemSlot, projectileItem.factory().create());
    }

    /**
     * Invoked when the perk is activated
     *
     * @param player The player that activated this perk
     */
    @Override
    public void onActivate(ArenaPlayer player) {
        Player p = player.getPlayer();
        ItemStack m = CompatibilityHandler.either(() -> p.getInventory().getItemInMainHand(), () -> p.getItemInHand());
        m.setAmount(m.getAmount() - 1);
        Projectile projectile = p.launchProjectile(projectileType.getProjectileClass());
        Metas.set(projectile, METADATA, player);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Projectile)) return;
        Projectile projectile = event.getEntity();
        if (!projectile.hasMetadata(METADATA)) return;
        ArenaPlayer shooter = Metas.get(projectile, METADATA);
        if (shooter.getCurrentArena() == null) return;
        if (!canUse(shooter.getCurrentArena().getExtension())) return;
        Block hitBlock = CompatibilityHandler.getHitBlock(null, event);
        GameArena arena = shooter.getCurrentArena();
        if (hitBlock != null)
            if (arena.getEngine().getArenaStage() == ArenaStage.ACTIVE)
                if (!nonDestroyableBlocks.contains(hitBlock.getType()))
                    if (CompatibilityHandler.getWorldGuardHook().canBreak(shooter.getPlayer(), hitBlock))
                        hitBlock.setType(Material.AIR);
        projectile.remove();
    }
}