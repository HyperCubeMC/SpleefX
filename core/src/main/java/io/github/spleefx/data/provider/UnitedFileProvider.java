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
package io.github.spleefx.data.provider;

import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.api.ArenaData;
import io.github.spleefx.data.DataProvider;
import io.github.spleefx.data.GameStats;
import io.github.spleefx.data.LeaderboardTopper;
import io.github.spleefx.data.PlayerStatistic;
import io.github.spleefx.economy.booster.BoosterInstance;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.util.plugin.PluginSettings;
import org.bukkit.OfflinePlayer;
import org.moltenjson.configuration.direct.DirectConfiguration;
import org.moltenjson.json.JsonFile;

import java.io.File;
import java.util.List;

public class UnitedFileProvider implements DataProvider {

    private DirectConfiguration config = DirectConfiguration.of
            (JsonFile.of(new File(SpleefX.getPlugin().getDataFolder(), PluginSettings.STATISTICS_DIRECTORY.get()),
                    PluginSettings.UNITED_FILE_NAME.get()));

    /**
     * Returns whether the player has an entry in the storage or not
     *
     * @param player Player to check for
     * @return {@code true} if the player is stored, false if otherwise.
     */
    @Override
    public boolean hasEntry(OfflinePlayer player) {
        return config.contains(DataProvider.getStoringStrategy().apply(player));
    }

    /**
     * Adds the player to the data entries
     *
     * @param player Player to add
     */
    @Override
    public void add(OfflinePlayer player) {
        config.set(DataProvider.getStoringStrategy().apply(player), new GameStats(), ArenaData.GSON);
    }

    public DirectConfiguration getConfig() {
        return config;
    }

    /**
     * Adds the specified booster to the player
     *
     * @param player  Player to give
     * @param booster Booster to add.
     */
    @Override
    public void addBooster(OfflinePlayer player, BoosterInstance booster) {
        if (!hasEntry(player))
            add(player);
        GameStats stats = config.get(DataProvider.getStoringStrategy().apply(player), GameStats.class, ArenaData.GSON);
        stats.getBoosters().put(stats.getBoosters().size(), booster);
        config.set(DataProvider.getStoringStrategy().apply(player), stats, ArenaData.GSON);
    }

    /**
     * Retrieves the player's statistics from the specified extension
     *
     * @param stat   Statistic to retrieve
     * @param player Player to retrieve from
     * @param mode   The mode. Set to {@code null} to get global statistics
     * @return The statistic
     */
    @Override
    public int get(PlayerStatistic stat, OfflinePlayer player, GameExtension mode) {
        if (hasEntry(player))
            return config.get(DataProvider.getStoringStrategy().apply(player), GameStats.class, ArenaData.GSON);
        add(player);
        return 0;
    }

    /**
     * Adds the specified amount to the statistic
     *
     * @param stat     Statistic to add to
     * @param player   Player to add for
     * @param mode     Mode to add for
     * @param addition Value to add
     */
    @Override
    public void add(PlayerStatistic stat, OfflinePlayer player, GameExtension mode, int addition) {
        if (!hasEntry(player))
            add(player);
        GameStats stats = config.get(DataProvider.getStoringStrategy().apply(player), GameStats.class, ArenaData.GSON);
        stats.add(stat, mode, addition);
        config.set(DataProvider.getStoringStrategy().apply(player), stats);
    }

    /**
     * Saves all the entries of the data
     *
     * @param plugin Plugin instance
     */
    @Override
    public void saveEntries(SpleefX plugin) {
        config.save(Throwable::printStackTrace, ArenaData.GSON);
    }

    /**
     * Sets the player statistics entirely. Useful for converting between different {@link DataProvider}
     * implementations.
     *
     * @param player Player to convert
     * @param stats  Stats to override with
     */
    @Override
    public void setStatistics(OfflinePlayer player, GameStats stats) {
        throw new AbstractMethodError();
    }

    /**
     * Returns the top n players in the specified statistic
     *
     * @param statistic Statistic to get from
     * @param extension
     */
    @Override public List<LeaderboardTopper> getTopPlayers(PlayerStatistic statistic, GameExtension extension) {
        throw new AbstractMethodError();
    }

    /**
     * Returns the statistics of the specified player
     *
     * @param player Player to retrieve from
     * @return The player's statistics
     */
    @Override
    public GameStats getStatistics(OfflinePlayer player) {
        GameStats stats = config.get(DataProvider.getStoringStrategy().apply(player), GameStats.class, ArenaData.GSON);
        if (stats == null)
            config.set(DataProvider.getStoringStrategy().apply(player), stats = new GameStats(), ArenaData.GSON);
        return stats;
    }

}