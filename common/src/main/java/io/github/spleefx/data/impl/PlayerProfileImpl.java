package io.github.spleefx.data.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import io.github.spleefx.SpleefX;
import io.github.spleefx.data.GameStatType;
import io.github.spleefx.data.PlayerProfile;
import io.github.spleefx.economy.booster.BoosterInstance;
import io.github.spleefx.extension.standard.splegg.SpleggExtension;
import io.github.spleefx.extension.standard.splegg.SpleggUpgrade;
import io.github.spleefx.perk.GamePerk;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import static io.github.spleefx.config.SpleefXConfig.ECO_USE_VAULT;
import static io.github.spleefx.config.SpleefXConfig.VAULT_EXISTS;
import static io.github.spleefx.data.impl.HikariConnector.GSON;

@ToString
public class PlayerProfileImpl implements PlayerProfile {

    private final UUID uuid;
    @Exclude private final OfflinePlayer offlineCopy;
    private final int coins;
    private final ImmutableMap<GameStatType, Integer> stats;
    private final ImmutableMap<String, Map<GameStatType, Integer>> modeStats;
    private final ImmutableMap<Integer, BoosterInstance> boosters;
    private final ImmutableMap<GamePerk, Integer> perks;
    private final ImmutableList<SpleggUpgrade> spleggUpgrades;
    private final List<String> spleggUpgradesKeys;
    private String selectedSpleggUpgrade;

    public PlayerProfileImpl(
            UUID uuid,
            int coins,
            ImmutableMap<GameStatType, Integer> stats,
            ImmutableMap<String, Map<GameStatType, Integer>> modeStats,
            ImmutableMap<Integer, BoosterInstance> boosters,
            ImmutableMap<GamePerk, Integer> perks,
            ImmutableList<SpleggUpgrade> spleggUpgrades,
            String selectedSpleggUpgrade) {
        this.uuid = uuid;
        offlineCopy = Bukkit.getOfflinePlayer(uuid);
        if (ECO_USE_VAULT.get() && VAULT_EXISTS) {
            coins = (int) SpleefX.getPlugin().getVaultHandler().getCoins(offlineCopy);
        }
        this.coins = coins;
        this.stats = n(stats, "stats");
        this.modeStats = n(modeStats, "modeStats");
        this.boosters = n(boosters, "boosters");
        this.perks = n(perks, "perks");
        this.spleggUpgrades = n(spleggUpgrades, "spleggUpgrades");
        spleggUpgradesKeys = spleggUpgrades.stream().map(SpleggUpgrade::getKey).collect(Collectors.toList());
        this.selectedSpleggUpgrade = n(selectedSpleggUpgrade, "selectedSpleggUpgrade");
    }

    @Override public int getCoins() {
        return coins;
    }

    @Override public @NotNull UUID getUUID() {
        return uuid;
    }

    @Override public @NotNull Map<GameStatType, Integer> getGameStats() {
        return stats;
    }

    @Override public @NotNull Map<String, Map<GameStatType, Integer>> getExtensionStatistics() {
        return modeStats;
    }

    @Override public @NotNull Map<Integer, BoosterInstance> getBoosters() {
        return boosters;
    }

    @Override public @NotNull String getSelectedSpleggUpgrade() {
        if (selectedSpleggUpgrade.equals("default"))
            return selectedSpleggUpgrade = SpleggExtension.EXTENSION.getUpgrades().values().stream()
                    .filter(SpleggUpgrade::isDefault).findFirst().get().getKey();
        return selectedSpleggUpgrade;
    }

    @NotNull @Override public Map<GamePerk, Integer> getPerks() {
        return perks;
    }

    @Override public @NotNull List<SpleggUpgrade> getPurchasedSpleggUpgrades() {
        return spleggUpgrades;
    }

    @Override public @NotNull List<String> upgradeKeys() {
        return spleggUpgradesKeys;
    }

    @Override public @NotNull List<BoosterInstance> getActiveBoosters() {
        return boosters.values().stream().filter(BoosterInstance::isActive).collect(Collectors.toList());
    }

    @Override public @NotNull Builder asBuilder() {
        return new BuilderImpl(this);
    }

    @Override public @NotNull String asPlaceholders() {
        return "(?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    @SuppressWarnings("PointlessArithmeticExpression") // more readable
    public int passToStatement(@NotNull UUID uuid, @NotNull PreparedStatement statement, final int index) throws SQLException {
        statement.setString(index + 0, uuid.toString());
        statement.setInt(index + 1, coins);
        statement.setString(index + 2, selectedSpleggUpgrade);
        statement.setString(index + 3, GSON.toJson(spleggUpgrades));
        statement.setString(index + 4, GSON.toJson(stats));
        statement.setString(index + 5, GSON.toJson(modeStats));
        statement.setString(index + 6, GSON.toJson(boosters));
        statement.setString(index + 7, GSON.toJson(perks));
        return 8;
    }

    public static class BuilderImpl implements Builder {

        protected UUID uuid;
        protected final OfflinePlayer offlineCopy;
        protected int coins = 0;
        protected Map<GameStatType, Integer> stats = new HashMap<>();
        protected Map<String, Map<GameStatType, Integer>> modeStats = new HashMap<>();
        protected Map<Integer, BoosterInstance> boosters = new HashMap<>();
        protected Map<GamePerk, Integer> perks = new HashMap<>();
        protected List<SpleggUpgrade> spleggUpgrades = new ArrayList<>();

        protected String spleggUpgrade;

        public BuilderImpl(UUID uuid) {
            this.uuid = uuid;
            offlineCopy = Bukkit.getOfflinePlayer(uuid);
        }

        public BuilderImpl(PlayerProfileImpl data) {
            uuid = data.uuid;
            coins = data.getCoins();
            stats = new HashMap<>(data.stats);
            modeStats = new HashMap<>(data.modeStats);
            boosters = new HashMap<>(data.boosters);
            perks = new HashMap<>(data.perks);
            spleggUpgrades = new ArrayList<>(data.spleggUpgrades);
            spleggUpgrade = data.selectedSpleggUpgrade;
            offlineCopy = Bukkit.getOfflinePlayer(uuid);
        }

        @Override public @NotNull Builder setCoins(int coins) {
            this.coins = coins;
            return this;
        }

        @Override public @NotNull Builder setCoins(IntFunction<Integer> modification) {
            coins = modification.apply(coins);
            return this;
        }

        @Override public @NotNull Builder addCoins(int coins) {
            if (ECO_USE_VAULT.get() && VAULT_EXISTS) {
                SpleefX.getPlugin().getVaultHandler().add(offlineCopy, coins);
                return this;
            }
            this.coins += coins;
            return this;
        }

        @Override public @NotNull Builder subtractCoins(int coins) {
            if (ECO_USE_VAULT.get() && VAULT_EXISTS) {
                SpleefX.getPlugin().getVaultHandler().withdraw(offlineCopy, coins);
                return this;
            }
            this.coins -= coins;
            return this;
        }

        @Override public int coins() {
            if (ECO_USE_VAULT.get() && VAULT_EXISTS) {
                coins = (int) SpleefX.getPlugin().getVaultHandler().getCoins(offlineCopy);
            }
            return coins;
        }

        @Override public @NotNull Map<GamePerk, Integer> perks() {
            return perks;
        }

        @Override public @NotNull Builder resetStats() {
            for (GameStatType type : GameStatType.values)
                stats.put(type, 0);
            modeStats.replaceAll((k, v) -> {
                v.replaceAll((n, e) -> 0);
                return v;
            });
            return this;
        }

        @Override public @NotNull Builder replaceStat(@NotNull GameStatType statType, @NotNull IntFunction<Integer> task) {
            n(statType);
            n(task);
            stats.compute(statType, (stat, value) -> task.apply(value == null ? 0 : value));
            return this;
        }

        @Override public @NotNull Builder setStats(@NotNull Map<GameStatType, Integer> stats) {
            this.stats = n(stats, "stats");
            return this;
        }

        @Override public @NotNull Builder setModeStats(@NotNull Map<String, Map<GameStatType, Integer>> stats) {
            n(stats, "stats");
            modeStats = stats;
            return this;
        }

        @Override public @NotNull Builder setBoosters(@NotNull Map<Integer, BoosterInstance> boosters) {
            this.boosters = n(boosters, "boosters");
            return this;
        }

        @Override
        public @NotNull Builder setPerks(@NotNull Map<GamePerk, Integer> perks) {
            this.perks = n(perks, "perks");
            return this;
        }

        @Override public @NotNull Builder addBooster(@NotNull BoosterInstance booster) {
            n(booster, "Booster");
            boosters.put(boosters.size() + 1, booster);
            return this;
        }

        @Override public @NotNull Builder addPurchase(@NotNull SpleggUpgrade spleggUpgrade) {
            n(spleggUpgrade, "Splegg upgrade");
            spleggUpgrades.add(spleggUpgrade);
            return this;
        }


        @Override public @NotNull Builder replaceExtensionStat(@NotNull String mode, @NotNull GameStatType statType, @NotNull IntFunction<Integer> task) {
            n(mode);
            n(statType);
            n(task);
            modeStats.computeIfAbsent(mode, k -> new HashMap<>()).compute(statType, (stat, value) -> task.apply(value == null ? 0 : value));
            replaceStat(statType, task);
            return this;
        }

        @Override public @NotNull Builder setSpleggUpgrade(@NotNull String upgrade) {
            spleggUpgrade = n(upgrade, "upgrade");
            return this;
        }

        @Override public @NotNull Builder setPurchasedSpleggUpgrades(@NotNull List<SpleggUpgrade> upgrades) {
            spleggUpgrades = n(upgrades, "upgrades");
            return this;
        }

        @Override public @NotNull PlayerProfile build() {
            return new PlayerProfileImpl(
                    uuid,
                    coins,
                    ImmutableMap.copyOf(stats),
                    ImmutableMap.copyOf(modeStats),
                    ImmutableMap.copyOf(boosters),
                    ImmutableMap.copyOf(perks),
                    ImmutableList.copyOf(spleggUpgrades),
                    spleggUpgrade);
        }

    }

    private static <T> void n(T value) {
        Objects.requireNonNull(value);
    }

    private static <T> T n(T value, String message) {
        Objects.requireNonNull(value, message);
        return value;
    }

    protected static class UpgradeAdapter implements JsonSerializer<List<SpleggUpgrade>>, JsonDeserializer<List<SpleggUpgrade>> {

        @Override
        public List<SpleggUpgrade> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            ImmutableList.Builder<SpleggUpgrade> b = ImmutableList.builder();
            for (JsonElement e : jsonElement.getAsJsonArray()) {
                b.add(SpleggExtension.EXTENSION.getUpgrades().get(e.getAsString()));
            }
            return b.build();
        }

        @Override
        public JsonElement serialize(List<SpleggUpgrade> spleggUpgrades, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonArray array = new JsonArray();
            for (SpleggUpgrade spleggUpgrade : spleggUpgrades) {
                array.add(new JsonPrimitive(spleggUpgrade.getKey()));
            }
            return array;
        }
    }
}