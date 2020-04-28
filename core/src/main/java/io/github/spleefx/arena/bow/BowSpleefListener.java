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
package io.github.spleefx.arena.bow;

import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.ArenaPlayer.ArenaPlayerState;
import io.github.spleefx.arena.ArenaStage;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.extension.standard.bowspleef.BowSpleefExtension;
import io.github.spleefx.util.game.Metas;
import io.github.spleefx.util.plugin.Protocol;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;

import static io.github.spleefx.arena.bow.BowSpleefEngine.ARROW_METADATA;

public class BowSpleefListener implements Listener {

    private SpleefX plugin;

    public BowSpleefListener(SpleefX plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        Player player = (Player) event.getEntity().getShooter();
        ArenaPlayer arenaPlayer = ArenaPlayer.adapt(player);
        if (arenaPlayer.getState() != ArenaPlayerState.IN_GAME) return;
        GameArena arena = arenaPlayer.getCurrentArena();
        if (!(arena instanceof BowSpleefArena)) return;
        Metas.set(event.getEntity(), ARROW_METADATA, new FixedMetadataValue(plugin, arena));
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Projectile)) return;
        Projectile arrow = event.getEntity();
        if (!arrow.hasMetadata(ARROW_METADATA)) return;
        BowSpleefArena arena = Metas.get(arrow, ARROW_METADATA);
        Entity hitEntity = CompatibilityHandler.getHitEntity(arena, event);
        if (hitEntity != null && BowSpleefExtension.EXTENSION.getBounceArrows()) return;
        if (hitEntity == null) arrow.remove();
        Block hitBlock = CompatibilityHandler.getHitBlock(arena, event);
        if (hitBlock != null && hitBlock.getType() == Material.TNT && BowSpleefExtension.EXTENSION.getRemoveTNTWhenPrimed())
            if (arena.getEngine().getArenaStage() == ArenaStage.ACTIVE)
                hitBlock.setType(Material.AIR);
            else
                arrow.remove();
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (Protocol.isNewerThan(11)) return; // 1.11.X, no need
        if (event.getEntityType() != EntityType.PLAYER) return;
        if (event.getCause() != DamageCause.PROJECTILE) return;
        Projectile p = (Projectile) event.getDamager();
        if (!p.hasMetadata(ARROW_METADATA)) return;
        ArenaPlayer player = ArenaPlayer.adapt((Player) event.getEntity());
        if (!(player.getCurrentArena() instanceof BowSpleefArena)) return;
        BowSpleefArena arena = player.getCurrentArena();
        arena.getDamageMap().put(p.getUniqueId(), player.getPlayer());
    }

    @EventHandler
    public void onEntityCombustByEntity(EntityCombustByEntityEvent event) {
        if (event.getCombuster().getType() == EntityType.ARROW) {
            if (event.getCombuster().hasMetadata(ARROW_METADATA))
                event.setCancelled(true);
        }
    }
}