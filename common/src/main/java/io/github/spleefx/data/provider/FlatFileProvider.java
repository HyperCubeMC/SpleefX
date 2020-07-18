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

import com.google.common.base.Stopwatch;
import io.github.spleefx.SpleefX;
import io.github.spleefx.data.DataProvider;
import io.github.spleefx.data.GameStats;
import io.github.spleefx.data.LeaderboardTopper;
import io.github.spleefx.data.PlayerStatistic;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.util.PlaceholderUtil;
import io.github.spleefx.util.io.FileManager;
import io.github.spleefx.util.plugin.PluginSettings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.moltenjson.configuration.tree.TreeConfiguration;
import org.moltenjson.configuration.tree.TreeConfigurationBuilder;
import org.moltenjson.configuration.tree.strategy.TreeNamingStrategy;
import org.moltenjson.utils.Gsons;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.util.Collections.reverseOrder;

public class FlatFileProvider implements DataProvider {

    private static final TreeNamingStrategy<OfflinePlayer> NAMING_STRATEGY = new PlayerNamingStrategy();

    private static final Map<PlayerStatistic, Set<LeaderboardTopper>> TOP = new HashMap<>();

    private static final Map<PlayerStatistic, Map<GameExtension, Set<LeaderboardTopper>>> TOP_BY_EXTENSION = new HashMap<>();

    private TreeConfiguration<OfflinePlayer, GameStats> statisticsTree =
            new TreeConfigurationBuilder<OfflinePlayer, GameStats>(new File(SpleefX.getPlugin().getDataFolder(), PluginSettings.STATISTICS_DIRECTORY.get()), NAMING_STRATEGY)
                    .setLazy(false)
                    .setDataMap(new HashMap<>())
                    .setGson(Gsons.DEFAULT)
                    .ignoreInvalidFiles(true)
                    .build();

    /**
     * Returns whether the player has an entry in the storage or not
     *
     * @param player Player to check for
     * @return {@code true} if the player is stored, false if otherwise.
     */
    @Override
    public boolean hasEntry(OfflinePlayer player) {
        return statisticsTree.hasData(player);
    }

    /**
     * Adds the player to the data entries
     *
     * @param player Player to add
     */
    @Override
    public void add(OfflinePlayer player) {
        try {
            statisticsTree.createIfAbsent(player, new GameStats(), "json");
        } catch (IOException e) {
            SpleefX.logger().severe("Failed to create statistics for player " + player.getName() + ". Error:");
            e.printStackTrace();
        }
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
        return statisticsTree.lazyLoad(player, GameStats.class, "json").get(stat, mode);
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
        add(player);
        statisticsTree.get(player).add(stat, mode, addition);
    }

    /**
     * Saves all the entries of the data
     *
     * @param plugin Plugin instance
     */
    @Override
    public void saveEntries(SpleefX plugin) {
        try {
            statisticsTree.lazySave();
        } catch (IOException e) {
            SpleefX.logger().severe("Failed to save player statistics. Error:");
            e.printStackTrace();
        }
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
        try {
            statisticsTree.create(player, stats, "json");
        } catch (IOException e) {
            SpleefX.logger().severe("Failed to convert player statistics. Error:");
            e.printStackTrace();
        }
    }

    /**
     * Creates the required files for this provider
     *
     * @param fileManager File manager instance
     */
    @Override
    public void createRequiredFiles(FileManager<SpleefX> fileManager) {
        if (PlaceholderUtil.PAPI && (boolean) PluginSettings.LEADERBOARDS.get()) {
            AtomicBoolean firstTime = new AtomicBoolean(true);
            SpleefX.logger().info("Leaderboards are enabled. Loading player data after 5 seconds to allow it to be sorted beforehand. This may take some time depending on the amount of data it has to process.");
            Bukkit.getScheduler().runTaskTimer(fileManager.getPlugin(), () -> {
                Stopwatch timer = Stopwatch.createStarted();
                Map<OfflinePlayer, GameStats> stats = new HashMap<>(statisticsTree.isDataLoaded() ? statisticsTree.getData() : statisticsTree.load(GameStats.class));
                for (PlayerStatistic ps : PlayerStatistic.values) {
                    if (firstTime.get())
                        SpleefX.logger().info("Sorting leaderboards for statistic " + ps);

                    List<Entry<OfflinePlayer, GameStats>> toSort = new ArrayList<>(stats.entrySet());
                    toSort.removeIf(e -> e.getKey() == null || e.getValue() == null);
                    toSort.sort(reverseOrder(Comparator.comparingInt(e -> e.getValue().get(ps, null))));
                    List<LeaderboardTopper> top = toSort.stream().map(player -> new LeaderboardTopper(player.getKey().getUniqueId(), player.getValue().get(ps, null))).collect(Collectors.toList());
                    TOP.put(ps, new HashSet<>(top));
                    Map<GameExtension, Set<LeaderboardTopper>> tops = new HashMap<>();
                    for (GameExtension extension : ExtensionsManager.EXTENSIONS.values()) {
                        if (firstTime.get())
                            SpleefX.logger().info("Sorting leaderboards for extension " + extension.getKey() + " in statistic " + ps);
                        List<LeaderboardTopper> topEx = stats.entrySet().stream()
                                .sorted(reverseOrder(Comparator.comparingInt(e -> e.getValue().get(ps, extension))))
                                .map(e -> new LeaderboardTopper(e.getKey().getUniqueId(), e.getValue().get(ps, extension)))
                                .collect(Collectors.toList());
                        tops.put(extension, new LinkedHashSet<>(topEx));
                    }
                    TOP_BY_EXTENSION.put(ps, tops);
                }
                SpleefX.logger().info("Finished loading and sorting all leaderboards in " + timer.elapsed(TimeUnit.MILLISECONDS) + " milliseconds.");
                firstTime.set(false);
                timer.stop();
            }, 0, 600 * 20);
        }
    }

    /**
     * Returns the top n players in the specified statistic
     *
     * @param statistic Statistic to get from
     */
    @Override
    public List<LeaderboardTopper> getTopPlayers(PlayerStatistic statistic, GameExtension extension) {
        if (!(boolean) PluginSettings.LEADERBOARDS.get())
            throw new IllegalStateException("Leaderboards are not enabled! Enable them in the config.yml.");
        if (!PlaceholderUtil.PAPI)
            throw new IllegalStateException("PlaceholderAPI is not found! Get PlaceholderAPI for leaderboards to work.");
        if (extension == null) {
            try {
                return new ArrayList<>(TOP.get(statistic));
            } catch (NullPointerException e) {
                e.printStackTrace();
                throw new IllegalStateException("The plugin hasn't started loading leaderboards data yet! Please wait. If this occured even after 5 seconds of loading the plugin, report (all of) the errors above. Otherwise, ignore it and wait!");
            }
        } else
            return new ArrayList<>(TOP_BY_EXTENSION.get(statistic).get(extension));
    }

    /**
     * Returns the statistics of the specified player
     *
     * @param player Player to retrieve from
     * @return The player's statistics
     */
    @Override
    public GameStats getStatistics(OfflinePlayer player) {
        GameStats stats = statisticsTree.lazyLoad(player, GameStats.class, "json");
        if (stats == null) {
            try {
                statisticsTree.create(player, stats = new GameStats(), "json");
            } catch (IOException e) {
                SpleefX.logger().severe("Failed to save player statistics. Error:");
                e.printStackTrace();
            }
        }
        return stats;
    }

    static class PlayerNamingStrategy implements TreeNamingStrategy<OfflinePlayer> {

        /**
         * Converts the specified object to be a valid file name. The returned file name
         * should NOT channelTo the extension.
         *
         * @param e Object to convert
         * @return The valid file name.
         */
        @Override
        public String toName(OfflinePlayer e) {
            return DataProvider.getStoringStrategy().apply(e);
        }

        /**
         * Converts the file name to be an object, can be used as a key.
         *
         * @param name The file name. This does <i>NOT</i> include the extension.
         * @return The object key
         */
        @Override
        public OfflinePlayer fromName(String name) {
            return DataProvider.getStoringStrategy().from(name);
        }
    }

}
