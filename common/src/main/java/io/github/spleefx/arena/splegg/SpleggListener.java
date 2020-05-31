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
package io.github.spleefx.arena.splegg;

import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.ArenaPlayer.ArenaPlayerState;
import io.github.spleefx.arena.ArenaStage;
import io.github.spleefx.arena.ModeType;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.data.GameStats;
import io.github.spleefx.data.PlayerStatistic;
import io.github.spleefx.extension.standard.splegg.SpleggUpgrade;
import io.github.spleefx.util.game.ExplosionSettings;
import io.github.spleefx.util.game.Metas;
import io.github.spleefx.util.plugin.Protocol;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import static io.github.spleefx.compatibility.CompatibilityHandler.getProtocol;
import static io.github.spleefx.extension.standard.splegg.SpleggExtension.EXTENSION;

public class SpleggListener implements Listener {

    public static final SpleggDelayHandler delayHandler = new SpleggDelayHandler();

    private SpleefX plugin;

    public SpleggListener(SpleefX plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem()) return;
        ArenaPlayer player = ArenaPlayer.adapt(event.getPlayer());
        if (player.getState() != ArenaPlayerState.IN_GAME) return;
        GameArena arena = player.getCurrentArena();
        if (arena.type != ModeType.SPLEGG) return;
        if (!EXTENSION.getClickActions().contains(event.getAction())) return;
        if (EXTENSION.isUpgradeSystemEnabled()) {
            if (delayHandler.has(player.getPlayer())) return;
            GameStats stats = player.getStats();
            SpleggUpgrade selected = EXTENSION.getUpgrades().getOrDefault((String) stats.getCustomDataMap().get("selectedSpleggUpgrade"),
                    EXTENSION.getUpgrades().values().stream().filter(SpleggUpgrade::isDefault).findFirst().orElse(null));
            if (selected == null) return;
            if (event.getItem().isSimilar(selected.getGameItem().createItem(player.getPlayer(), selected))) {
                delayHandler.delay(event.getPlayer(), selected.getDelay());
                Projectile p = event.getPlayer().launchProjectile(EXTENSION.getProjectileType().getProjectileClass());
                if (p instanceof Fireball) {
                    ((Fireball) p).setYield(0F);
                }
                p.getVelocity().add(new Vector(0.1, 0.1, 0.1));
                Metas.set(p, "spleefx.splegg.projectile", new FixedMetadataValue(plugin, arena));
            }
        } else {
            if (event.hasItem() && event.getItem().isSimilar(EXTENSION.getProjectileItem().factory().create())) {
                Projectile p = event.getPlayer().launchProjectile(EXTENSION.getProjectileType().getProjectileClass());
                if (p instanceof Fireball) {
                    ((Fireball) p).setYield(0F);
                }
                Metas.set(p, "spleefx.splegg.projectile", new FixedMetadataValue(plugin, arena));
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!event.getEntity().hasMetadata("spleefx.splegg.projectile")) return;
        SpleggArena arena = Metas.get(event.getEntity(), "spleefx.splegg.projectile");
        Block hitBlock = CompatibilityHandler.getHitBlock(arena, event);
        if (hitBlock != null && EXTENSION.isDestroyable(hitBlock.getType())) {
            Location loc = hitBlock.getLocation();
            if (arena.getEngine().getArenaStage() == ArenaStage.ACTIVE) {
                ExplosionSettings explosionSettings = EXTENSION.getExplodeTNTWhenHit();
                if (hitBlock.getType() == Material.TNT && explosionSettings != null && explosionSettings.isEnabled()) {
                    hitBlock.setType(Material.AIR);
                    getProtocol().createExplosion(loc, explosionSettings);
                } else
                    hitBlock.setType(Material.AIR);
            } else
                event.getEntity().remove();
            SpleefX.getPlugin().getDataProvider().add(PlayerStatistic.BLOCKS_MINED, ((Player) event.getEntity().getShooter()), EXTENSION, 1);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (Protocol.isNewerThan(11)) return; // 1.11+, no need
        if (event.getEntityType() != EntityType.PLAYER) return;
        if (event.getCause() != DamageCause.PROJECTILE) return;
        Projectile p = (Projectile) event.getDamager();
        if (!p.hasMetadata("spleefx.splegg.projectile")) return;
        ArenaPlayer player = ArenaPlayer.adapt((Player) event.getEntity());
        if (player.getCurrentArena().type != ModeType.SPLEGG) return;
        SpleggArena arena = player.getCurrentArena();
        arena.getDamageMap().put(p.getUniqueId(), player.getPlayer());
    }

}