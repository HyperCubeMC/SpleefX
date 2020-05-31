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
package io.github.spleefx.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import io.github.spleefx.SpleefX;
import io.github.spleefx.economy.booster.BoosterInstance;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.perk.GamePerk;
import io.github.spleefx.perk.GamePerk.MapAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import static io.github.spleefx.util.plugin.PluginSettings.ECO_USE_VAULT;

/**
 * A simple container (POJO) for a player's statistics. Used mainly by data providers which use GSON.
 */
public class GameStats {

    public static final AtomicBoolean VAULT_EXISTS = new AtomicBoolean(Bukkit.getPluginManager().getPlugin("Vault") != null);

    public static final NumberFormat FORMAT = NumberFormat.getInstance(Locale.US);

    /**
     * The player's coins
     */
    @Expose
    @SerializedName("coins")
    public int coins = 0;

    @Expose
    @JsonAdapter(MapAdapter.class)
    @SerializedName("perks")
    private Map<GamePerk, Integer> perks = new HashMap<>();

    /**
     * A list of the player's boosters
     */
    @Expose
    @SerializedName("boosters")
    private Map<Integer, BoosterInstance> boosters = new HashMap<>();

    /**
     * Represents the global statistics
     */
    @Expose
    @SerializedName("global")
    private Map<PlayerStatistic, Integer> global;

    /**
     * A map which stores outer objects, whether from this plugin or from other plugins
     */
    @Expose
    @SerializedName("custom")
    private Map<Object, Object> customDataMap;

    /**
     * Represents statistics for each mode
     */
    @Expose
    @SerializedName("modes")
    private Map<String, Map<PlayerStatistic, Integer>> gameStatistics;

    /**
     * A simple instance for empty maps
     */
    public GameStats() {
        global = new HashMap<>();
        gameStatistics = new HashMap<>();
    }

    /**
     * Returns the specified statistic
     *
     * @param type Statistic to get
     * @param mode Mode to get from. Null to get global statistics
     * @return The statistic
     */
    public int get(PlayerStatistic type, GameExtension mode) {
        if (mode == null)
            return global.computeIfAbsent(type, (e) -> 0);
        return gameStatistics.computeIfAbsent(mode.getKey(), (v) -> new HashMap<>()).computeIfAbsent(type, (e) -> 0);
    }

    /**
     * Adds the specified statistics to the player
     *
     * @param type     Type of the statistic
     * @param mode     Mode to add. Can be null.
     * @param addition Value to add
     * @return
     */
    public GameStats add(PlayerStatistic type, GameExtension mode, int addition) {
        global.merge(type, addition, Integer::sum);
        if (mode != null)
            gameStatistics.computeIfAbsent(mode.getKey(), (v) -> new HashMap<>()).merge(type, addition, Integer::sum);
        return this;
    }

    public Map<Object, Object> getCustomDataMap() {
        return customDataMap == null ? customDataMap = new HashMap<>() : customDataMap;
    }

    public List<BoosterInstance> getActiveBoosters() {
        return boosters.values().stream().filter(BoosterInstance::isActive).collect(Collectors.toList());
    }

    public Map<GamePerk, Integer> getPerks() {
        return perks;
    }

    /**
     * Returns the amount of coins the player has
     *
     * @return The coins
     */
    public int getCoins(OfflinePlayer player) {
        if ((boolean) ECO_USE_VAULT.get() && VAULT_EXISTS.get()) {
            coins = (int) SpleefX.getPlugin().getVaultHandler().getCoins(player);
        }
        return coins;
    }

    /**
     * Applies the specified task on the player's coins
     *
     * @param task Task to run
     */
    public int onCoins(IntFunction<Integer> task) {
        return coins = task.apply(coins);
    }

    public void giveCoins(OfflinePlayer player, int amount) {
        if ((boolean) ECO_USE_VAULT.get() && VAULT_EXISTS.get()) {
            SpleefX.getPlugin().getVaultHandler().add(player, amount);
            return;
        }
        coins += amount;
    }

    /**
     * Returns the amount of coins the player has
     *
     * @return The coins
     */
    public String getCoinsFormatted(OfflinePlayer player) {
        return FORMAT.format(getCoins(player));
    }

    public void takeCoins(OfflinePlayer player, int amount) {
        if ((boolean) ECO_USE_VAULT.get() && VAULT_EXISTS.get()) {
            SpleefX.getPlugin().getVaultHandler().withdraw(player, amount);
            return;
        }
        coins -= amount;
    }

    /**
     * Returns the player's boosters
     *
     * @return The boosters
     */
    public Map<Integer, BoosterInstance> getBoosters() {
        return boosters;
    }

    @Override
    public String toString() {
        return "GameStats{" +
                "coins=" + coins +
                ", boosters=" + boosters +
                ", global=" + global +
                ", gameStatistics=" + gameStatistics +
                '}';
    }

}
