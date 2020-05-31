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
package io.github.spleefx.arena.spleef;

import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.api.BaseArenaEngine;
import io.github.spleefx.arena.api.GameTask;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.compatibility.material.MaterialCompatibility;
import io.github.spleefx.util.Percentage;
import io.github.spleefx.util.game.InventoryUtils;
import io.github.spleefx.util.plugin.PluginSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.spleefx.arena.spleef.SpleefArena.EXTENSION;
import static java.util.Comparator.comparingInt;

/**
 * A custom engine for spleef games, to add melting
 */
public class SpleefEngine extends BaseArenaEngine<SpleefArena> {

    private static final Random RANDOM = new Random();

    private BukkitTask meltingTask;

    private MeltingTask task;

    /**
     * Creates an engine for the specified arena
     *
     * @param arena Arena to create for
     */
    public SpleefEngine(SpleefArena arena) {
        super(arena);
        task = new MeltingTask(this);
        registerEndTask(new ClearListTask(this));
    }

    @Override
    public void loop() {
        super.loop();
        meltingTask = Bukkit.getScheduler().runTaskTimer(SpleefX.getPlugin(), task = new MeltingTask(this),
                ((Integer) PluginSettings.ARENA_MELTING_INTERVAL.get()).longValue(), ((Integer) PluginSettings.ARENA_MELTING_INTERVAL.get()).longValue());
    }

    /**
     * A simple implementation for the melting
     */
    static class MeltingTask extends GameTask {

        /**
         * Compares locations to check if they are equal
         */
        private static final Comparator<Location> LOCATION_COMPARATOR =
                comparingInt((Location location) -> PluginSettings.ARENA_MELTING_IGNORE_X.get() ? 0 : location.getBlockX())
                        .thenComparingInt(location -> PluginSettings.ARENA_MELTING_IGNORE_Y.get() ? 0 : location.getBlockY())
                        .thenComparingInt(location -> PluginSettings.ARENA_MELTING_IGNORE_Z.get() ? 0 : location.getBlockZ());

        /**
         * All locations of players
         */
        private Map<UUID, Location> locations = new HashMap<>();

        /**
         * Represents the engine
         */
        private SpleefEngine engine;

        /**
         * A list of all meltable materials
         */
        private List<Material> meltableBlocks;

        /**
         * Creates a melting task for the specified engine
         *
         * @param engine Engine to add for
         */
        public MeltingTask(SpleefEngine engine) {
            super(Phase.AFTER);
            this.engine = engine;
            List<String> l = PluginSettings.ARENA_MELTING_BLOCKS.get();
            meltableBlocks = l.stream().map(Material::matchMaterial).collect(Collectors.toList());
            meltableBlocks.removeIf(Objects::isNull);
        }

        @Override
        public void run() {
            if (engine.arena.isMelt() && ((int) PluginSettings.ARENA_MELTING_RADIUS.get()) != 0) {
                for (ArenaPlayer a : engine.getPlayerTeams().keySet()) {
                    Player player = a.getPlayer();
                    Location prev = locations.put(player.getUniqueId(), player.getLocation());
                    if (prev == null) continue;
                    if (LOCATION_COMPARATOR.compare(prev, player.getLocation()) != 0)
                        continue; // Player is in a different location
                    Block b = pickBlock(getLowestBlock(player.getLocation()).getLocation(), PluginSettings.ARENA_MELTING_RADIUS.get());
                    if (b == null) continue; // No meltable block found
                    b.setType(Material.AIR);
                    if (EXTENSION.getSnowballSettings().removeSnowballsGraduallyOnMelting()) {
                        Percentage p = EXTENSION.getSnowballSettings().getRemovalChance();
                        if (p.isApplicable())
                            InventoryUtils.removeItem(player.getInventory(), new ItemStack(CompatibilityHandler.getMaterialCompatibility().get(MaterialCompatibility.SNOWBALL), EXTENSION.getSnowballSettings().getRemovedAmount()), EXTENSION.getSnowballSettings().getRemovedAmount());
                    }
                }
            }
        }

        private static Block getLowestBlock(Location location) {
            Block lowestBlock = null;
            for (int y = location.getBlockY() - 1; y > 0; y--) {
                lowestBlock = location.getWorld().getBlockAt(location.add(0, -0.5, 0));
                if (lowestBlock.getType() != Material.AIR) {
                    return lowestBlock;
                }
            }
            return lowestBlock;
        }

        /**
         * Picks a random snow block in the specified location from the specified radius
         *
         * @param location Location to get from
         * @param radius   Radius to get from
         * @return The block, or null if none is found
         */
        private Block pickBlock(Location location, int radius) {
            final int x = location.getBlockX();
            final int y = (int) Math.round(location.getY());
            final int z = location.getBlockZ();
            final int minX = x - radius;
            final int minZ = z - radius;
            final int maxX = x + radius;
            final int maxZ = z + radius;
            List<Block> blocks = new ArrayList<>();
            for (int counterX = minX; counterX <= maxX; counterX++) {
                for (int counterZ = minZ; counterZ <= maxZ; counterZ++) {
                    Block block = location.getWorld().getBlockAt(counterX, y, counterZ);
                    if (!meltableBlocks.contains(block.getType())) continue;
                    blocks.add(block);
                }
            }
            return blocks.size() == 0 ? null : blocks.get(RANDOM.nextInt(blocks.size()));
        }

    }

    static class ClearListTask extends GameTask {

        /**
         * The task to clear
         */
        private SpleefEngine engine;

        /**
         * Creates a new task
         *
         * @param engine Engine to handle for
         */
        public ClearListTask(SpleefEngine engine) {
            super(Phase.BEFORE);
            this.engine = engine;
        }

        @Override
        public void run() {
            if (engine.task != null)
                engine.task.locations.clear();
            if (engine.meltingTask != null)
                engine.meltingTask.cancel();
        }
    }
}