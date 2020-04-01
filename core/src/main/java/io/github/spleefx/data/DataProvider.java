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

import io.github.spleefx.SpleefX;
import io.github.spleefx.data.provider.FlatFileProvider;
import io.github.spleefx.economy.booster.BoosterInstance;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.util.io.FileManager;
import io.github.spleefx.util.plugin.PluginSettings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;

import java.util.List;

import static java.util.UUID.fromString;

/**
 * A simple abstract class for providing high-level access to player data
 */
public interface DataProvider {

    /**
     * Creates the required files for this provider
     *
     * @param fileManager File manager instance
     */
    default void createRequiredFiles(FileManager<SpleefX> fileManager) {
    }

    /**
     * Returns whether the player has an entry in the storage or not
     *
     * @param player Player to check for
     * @return {@code true} if the player is stored, false if otherwise.
     */
    boolean hasEntry(OfflinePlayer player);

    /**
     * Adds the player to the data entries
     *
     * @param player Player to add
     */
    void add(OfflinePlayer player);

    /**
     * Retrieves the player's statistics from the specified extension
     *
     * @param stat   Statistic to retrieve
     * @param player Player to retrieve from
     * @param mode   The mode. Set to {@code null} to get global statistics
     * @return The statistic
     */
    int get(PlayerStatistic stat, OfflinePlayer player, GameExtension mode);

    /**
     * Adds the specified amount to the statistic
     *
     * @param stat      Statistic to add to
     * @param player    Player to add for
     * @param mode      Mode to add for
     * @param increment Value to add
     */
    void add(PlayerStatistic stat, OfflinePlayer player, GameExtension mode, int increment);

    /**
     * Adds the specified booster to the player
     * <p>
     * Note: This method <i>MUST</i> be overriden if the data provider does not cache!
     *
     * @param player  Player to give
     * @param booster Booster to add.
     */
    default void addBooster(OfflinePlayer player, BoosterInstance booster) {
        GameStats stats = getStatistics(player);
        stats.getBoosters().put(stats.getBoosters().size() + 1, booster);
    }

    /**
     * Saves all the entries of the data
     *
     * @param plugin Plugin instance
     */
    void saveEntries(SpleefX plugin);

    /**
     * Sets the player statistics entirely. Useful for converting between different {@link DataProvider}
     * implementations.
     *
     * @param player Player to convert
     * @param stats  Stats to override with
     */
    void setStatistics(OfflinePlayer player, GameStats stats);

    /**
     * Returns the top n players in the specified statistic
     *
     * @param statistic Statistic to get from
     */
    List<LeaderboardTopper> getTopPlayers(PlayerStatistic statistic, GameExtension extension);

    /**
     * Returns the statistics of the specified player
     *
     * @param player Player to retrieve from
     * @return The player's statistics
     */
    GameStats getStatistics(OfflinePlayer player);

    /**
     * Returns the storing strategy specified in the config
     *
     * @return The strategy
     */
    static PlayerStoringStrategy getStoringStrategy() {
        return PluginSettings.STATISTICS_STORE_PLAYERS_BY.get();
    }

    /**
     * Creates a GUI for the player's statistics
     *
     * @param of   Player to get for
     * @param mode The mode
     * @return The created inventory
     */
    default Inventory createGUI(OfflinePlayer of, GameExtension mode) {
        GameStats s = getStatistics(of);
        return StatisticsConfig.MENU.get().asInventory(of, s, mode);
    }

    /**
     * Represents the storage type
     */
    enum StorageType {

        /**
         * A united file for all stats
         */
        UNITED_FILE(FlatFileProvider.class),

        /**
         * A file for each player
         */
        FLAT_FILE(FlatFileProvider.class),

        /**
         * A SQLite file
         */
        SQLITE(getSQLProvider());

        private Class<? extends DataProvider> providerClass;

        /**
         * Registers a new data provider
         *
         * @param providerClass The implementation class for the storage type
         */
        StorageType(Class<? extends DataProvider> providerClass) {
            this.providerClass = providerClass;
        }

        /**
         * Creates a new instance of the data provider
         *
         * @param <R> The provider reference
         * @return The provider
         */
        @SuppressWarnings("unchecked")
        public <R extends DataProvider> R create() {
            try {
                return (R) providerClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Returns the SQL provider class, or the flat file provider if no SQL provider is found
         *
         * @return The appropriate class
         */
        @SuppressWarnings("unchecked")
        private static Class<? extends DataProvider> getSQLProvider() {
            try {
                return (Class<? extends DataProvider>) Class.forName("io.github.spleefx.data.provider.sqlite.SQLiteProvider");
            } catch (Throwable t) {
                return FlatFileProvider.class;
            }
        }

    }

    /**
     * How players should be stored
     */
    enum PlayerStoringStrategy {

        /**
         * Store by their UUID
         */
        UUID {
            @Override
            public String apply(OfflinePlayer player) {
                return player.getUniqueId().toString();
            }

            @Override
            public OfflinePlayer from(String value) {
                return Bukkit.getOfflinePlayer(fromString(value));
            }
        },

        /**
         * Store by their username
         */
        NAME {
            @Override
            public String apply(OfflinePlayer player) {
                return player.getName();
            }

            @Override
            public OfflinePlayer from(String value) {
                return Bukkit.getOfflinePlayer(value);
            }
        };

        /**
         * Applies the strategy and returns the valid string
         *
         * @param player Player to retrieve for
         * @return The string of this strategy
         */
        public abstract String apply(OfflinePlayer player);

        /**
         * Applies the strategy and returns the valid string
         *
         * @param value Value to retrieve from
         * @return The string of this strategy
         */
        public abstract OfflinePlayer from(String value);

    }

}
