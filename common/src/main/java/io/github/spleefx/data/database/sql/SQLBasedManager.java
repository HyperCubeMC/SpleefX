package io.github.spleefx.data.database.sql;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.google.common.collect.ImmutableSet;
import com.zaxxer.hikari.HikariConfig;
import io.github.spleefx.SpleefX;
import io.github.spleefx.config.SpleefXConfig;
import io.github.spleefx.data.PlayerCacheManager;
import io.github.spleefx.data.PlayerProfile;
import io.github.spleefx.data.impl.HikariConnector;
import io.github.spleefx.extension.standard.splegg.SpleggUpgrade;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.UUID;

public class SQLBasedManager extends HikariConnector implements PlayerCacheManager {

    public static final SQLBasedManager MYSQL = new SQLBasedManager("mysql", "com.mysql.cj.jdbc.Driver");
    public static final SQLBasedManager POSTGRESQL = new SQLBasedManager("postgresql", "org.postgresql.Driver");
    public static final SQLBasedManager H2 = new SQLBasedManager("h2", "org.h2.Driver") {
        @Override protected String createJdbcURL() {
            File file = new File(SpleefX.getPlugin().getDataFolder() + File.separator + "players-data",
                    SpleefXConfig.DB_NAME.get());
            return "jdbc:h2:" + file.getAbsolutePath() + ";DATABASE_TO_UPPER=false";
        }

        @Override protected void setCredentials(HikariConfig config) {
        }
    };
/*
    public static final SQLBasedManager SQLITE = new SQLBasedManager("sqlite") {
        private String path;

        @Override protected void preConnect() {
            String name = "player-data" + File.separator + pluginConfig.getString("SQLite.FileName", "player-data.db");
            path = SpleefX.getPlugin().getFileManager().emptyFile(name).getAbsolutePath();
        }

        @Override public boolean async() {
            return false;
        }

        @Override protected void setCredentials(HikariConfig config, ) {
            // sqlite has no credentials. default implementation sets them, so we don't want that.
        }

        @Override protected String createJdbcURL() {
            return "jdbc:sqlite:" + path;
        }
    };
*/

    public SQLBasedManager(String name, String driver) {
        super(name, driver);
    }

    @Nullable @Override
    public PlayerProfile load(@NonNull UUID key) {
        try {
            ResultSet set = prepare(StatementKey.SELECT_PLAYER, key.toString()).executeQuery();

            if (set.next()) {
                UUID uuid = UUID.fromString(set.getString("PlayerUUID"));
                return PlayerProfile.builder(uuid)
                        .setCoins(set.getInt("Coins"))
                        .setSpleggUpgrade(set.getString("SpleggUpgrade"))
                        .setPurchasedSpleggUpgrades(ImmutableSet.copyOf((List<SpleggUpgrade>) GSON.fromJson(set.getString("PurchasedSpleggUpgrades"), UPGRADES_TYPE)))
                        .setStats(GSON.fromJson(set.getString("GlobalStats"), GLOBAL_STATS_TYPE))
                        .setModeStats(GSON.fromJson(set.getString("ExtensionStats"), EXT_STATS_TYPE))
                        .setBoosters(GSON.fromJson(set.getString("Boosters"), BOOSTERS_TYPE))
                        .setPerks(GSON.fromJson(set.getString("Perks"), PERKS_TYPE))
                        .build();
            }
            if (!set.isClosed())
                set.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void cacheAll(LoadingCache<UUID, PlayerProfile> cache) {
        ResultSet set = executeQuery(StatementKey.BULK_SELECT_PLAYERS);
        try {
            while (set.next()) {
                UUID uuid = UUID.fromString(set.getString("PlayerUUID"));
                cache.put(uuid, PlayerProfile.builder(uuid)
                        .setCoins(set.getInt("Coins"))
                        .setSpleggUpgrade(set.getString("SpleggUpgrade"))
                        .setPurchasedSpleggUpgrades(GSON.fromJson(set.getString("PurchasedSpleggUpgrades"), UPGRADES_TYPE))
                        .setStats(GSON.fromJson(set.getString("GlobalStats"), GLOBAL_STATS_TYPE))
                        .setModeStats(GSON.fromJson(set.getString("ExtensionStats"), EXT_STATS_TYPE))
                        .setBoosters(GSON.fromJson(set.getString("Boosters"), BOOSTERS_TYPE))
                        .setPerks(GSON.fromJson(set.getString("Perks"), PERKS_TYPE))
                        .build());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override public void writeAll(@NotNull Map<UUID, PlayerProfile> map) {
        try {
            if (map.isEmpty()) return;
            StringJoiner joiner = new StringJoiner(",").setEmptyValue("");
            for (Entry<UUID, PlayerProfile> entry : map.entrySet()) {
                if (!entry.getValue().modified()) continue;
                joiner.add(entry.getValue().asPlaceholders());
            }
            if (joiner.toString().isEmpty()) return;
            PreparedStatement statement = connection.prepareStatement(String.format(schemas.get(StatementKey.UPSERT_PLAYER), joiner.toString()));
            int i = 1;
            for (Entry<UUID, PlayerProfile> entry : map.entrySet()) {
                if (!entry.getValue().modified()) continue;
                i += entry.getValue().passToStatement(entry.getKey(), statement, i);
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override public void write(@NonNull UUID key, @NonNull PlayerProfile value) {
        try {
            PreparedStatement statement = connection.prepareStatement(String.format(schemas.get(StatementKey.UPSERT_PLAYER), value.asPlaceholders()));
            value.passToStatement(key, statement, 1);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override public void delete(@NonNull UUID key, @Nullable PlayerProfile value, @NonNull RemovalCause cause) {
        try {
            prepare(StatementKey.DELETE_PLAYER, key.toString()).executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}