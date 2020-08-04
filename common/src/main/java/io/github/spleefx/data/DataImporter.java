package io.github.spleefx.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import io.github.spleefx.SpleefX;
import io.github.spleefx.config.SpleefXConfig;
import io.github.spleefx.data.impl.ForwardingCacheManager;
import io.github.spleefx.economy.booster.BoosterInstance;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.extension.standard.splegg.SpleggExtension;
import io.github.spleefx.perk.GamePerk;
import io.github.spleefx.perk.GamePerk.MapAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.moltenjson.configuration.tree.TreeConfiguration;
import org.moltenjson.configuration.tree.TreeConfigurationBuilder;
import org.moltenjson.configuration.tree.strategy.TreeNamingStrategy;
import org.moltenjson.utils.Gsons;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static io.github.spleefx.util.PlaceholderUtil.NUMBER_FORMAT;

/**
 * Class for handling importing old data from the previous format to the newer one
 */
public class DataImporter {

    private static final TreeNamingStrategy<OfflinePlayer> NAMING_STRATEGY = new PlayerNamingStrategy();

    static class PlayerNamingStrategy implements TreeNamingStrategy<OfflinePlayer> {

        @Override
        public String toName(OfflinePlayer e) {
            return e.getUniqueId().toString();
        }

        @Override
        public OfflinePlayer fromName(String name) {
            return Bukkit.getPlayer(UUID.fromString(name));
        }
    }

    private PlayerProfile convert(UUID uuid, GameStats old) {
        List<String> purchased = (List<String>) old.getCustomDataMap().getOrDefault("purchasedSpleggUpgrades", Collections.emptyList());

        return PlayerProfile.builder(uuid)
                .setCoins(old.coins)
                .setSpleggUpgrade(String.valueOf(old.getCustomDataMap().getOrDefault("selectedSpleggUpgrade", "default")))
                .setPurchasedSpleggUpgrades(purchased.stream().map(k -> SpleggExtension.EXTENSION.getUpgrades().get(k)).collect(Collectors.toSet()))
                .setModeStats(old.gameStatistics)
                .setStats(old.global)
                .setPerks(old.perks)
                .setBoosters(old.boosters)
                .build();
    }

    public void start() {
        SpleefX.logger().info("Using storage type " + SpleefXConfig.STORAGE_METHOD.get().getName() + ".");
        SpleefXConfig.STORAGE_METHOD.get().delegate();
        try {
            TreeConfiguration<UUID, GameStats> statisticsTree = new TreeConfigurationBuilder<UUID, GameStats>(new File(SpleefX.getPlugin().getDataFolder(), "player-data"), TreeNamingStrategy.UUID_STRATEGY)
                    .setLazy(false)
                    .setDataMap(new HashMap<>())
                    .setGson(Gsons.DEFAULT)
                    .ignoreInvalidFiles(false)
                    .build();
            if (!statisticsTree.getDirectory().exists()) return;
            statisticsTree.load(GameStats.class);
            Map<UUID, PlayerProfile> newData = new HashMap<>();
            int size = statisticsTree.getData().size();
            for (Entry<UUID, GameStats> data : statisticsTree.getData().entrySet()) {
                newData.put(data.getKey(), convert(data.getKey(), data.getValue()));
            }
            ForwardingCacheManager.delegate().writeAll(newData);
            SpleefX.logger().info("Finished importing " + NUMBER_FORMAT.format(size) + " entry(ies) to the newer format. Deleting old directory");
            File dir = statisticsTree.getDirectory();
            try {
                for (File file : dir.listFiles()) {
                    file.delete();
                }
                Files.delete(dir.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * A simple container (POJO) for a player's statistics. Used mainly by data providers which use GSON.
     */
    public static class GameStats {

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
        private Map<GameStatType, Integer> global;

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
        private Map<String, Map<GameStatType, Integer>> gameStatistics;

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
        public int get(GameStatType type, GameExtension mode) {
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
        public GameStats add(GameStatType type, GameExtension mode, int addition) {
            global.merge(type, addition, Integer::sum);
            if (mode != null)
                gameStatistics.computeIfAbsent(mode.getKey(), (v) -> new HashMap<>()).merge(type, addition, Integer::sum);
            return this;
        }

        public Map<Object, Object> getCustomDataMap() {
            return customDataMap == null ? customDataMap = new HashMap<>() : customDataMap;
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

}
