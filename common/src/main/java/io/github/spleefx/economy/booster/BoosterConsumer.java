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
package io.github.spleefx.economy.booster;

import io.github.spleefx.SpleefX;
import io.github.spleefx.data.PlayerProfile;
import io.github.spleefx.data.PlayerRepository;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A utility for handling all active boosters
 */
public class BoosterConsumer {

    /**
     * A map of all active boosters
     */
    private final Map<BoosterInstance, Long> activeBoosters = new HashMap<>();

    /**
     * The consuming task
     */
    private BukkitTask consumingTask;

    /**
     * Registers the booster to be consumed
     *
     * @param booster Booster to consume
     */
    public void consumeBooster(OfflinePlayer player, BoosterInstance booster) {
        activeBoosters.put(booster, booster.getDuration());
        PlayerProfile s = PlayerRepository.REPOSITORY.lookup(player);
        SpleefX.getActiveBoosterLoader().getActiveBoostersMap().put(booster.getOwner(), s.getBoosters().size());
    }

    /**
     * Stops the booster from being consumed
     *
     * @param booster Booster to pause
     */
    public void pauseBooster(BoosterInstance booster) {
        activeBoosters.remove(booster);
    }

    /**
     * Consumes from all the active boosters
     */
    public void consume() {
        for (Iterator<Entry<BoosterInstance, Long>> iterator = activeBoosters.entrySet().iterator(); iterator.hasNext(); ) {
            Entry<BoosterInstance, Long> boosterInstanceLongEntry = iterator.next();
            boosterInstanceLongEntry.getKey().reduce();
            if (boosterInstanceLongEntry.setValue(boosterInstanceLongEntry.getValue() - 1) <= 0) {
                iterator.remove();
                OfflinePlayer player = Bukkit.getOfflinePlayer(boosterInstanceLongEntry.getKey().getOwner());
                PlayerProfile stats = PlayerRepository.REPOSITORY.lookup(player);
                int boosterId = 0;
                for (Iterator<Entry<Integer, BoosterInstance>> iter = stats.getBoosters().entrySet().iterator(); iter.hasNext(); ) {
                    Entry<Integer, BoosterInstance> entry = iter.next();
                    Integer id = entry.getKey();
                    BoosterInstance booster = entry.getValue();
                    if (booster.equals(boosterInstanceLongEntry.getKey())) {
                        boosterId = id;
                        iter.remove();
                    }
                }
                SpleefX.getActiveBoosterLoader().getActiveBoostersMap().remove(player.getUniqueId(), boosterId);
            }
        }
    }

    /**
     * Starts the consuming task
     *
     * @param plugin Plugin to start for
     */
    public void start(Plugin plugin) {
        consumingTask = Bukkit.getScheduler().runTaskTimer(plugin, this::consume, 20, 20);
    }

    /**
     * Cancels the booster consuming task
     */
    public void cancel() {
        if (consumingTask != null)
            consumingTask.cancel();
    }

}
