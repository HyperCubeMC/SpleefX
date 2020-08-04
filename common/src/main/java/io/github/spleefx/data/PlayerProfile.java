package io.github.spleefx.data;

import io.github.spleefx.data.database.sql.SQLSerializable;
import io.github.spleefx.data.impl.PlayerProfileImpl.BuilderImpl;
import io.github.spleefx.economy.booster.BoosterInstance;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.extension.standard.splegg.SpleggUpgrade;
import io.github.spleefx.perk.GamePerk;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.IntFunction;

/**
 * Represents the data of a player (immutable)
 * <p>
 * This class is thread-safe.
 *
 * @see Builder
 * @see PlayerRepository
 */
public interface PlayerProfile extends SQLSerializable {

    /**
     * Returns the UUID of this player
     *
     * @return The player's UUID
     */
    @NotNull
    UUID getUUID();

    boolean modified();

    /**
     * Returns the player's coins
     *
     * @return The coins
     */
    int getCoins();

    /**
     * Returns per-stat game statistics
     *
     * @return Statistics
     */
    @NotNull
    Map<GameStatType, Integer> getGameStats();

    /**
     * Returns per-extension statistics
     *
     * @return Per-game mode stats
     * @see #getExtensionStatistics(String)
     * @see #getExtensionStatistics(GameExtension)
     */
    @NotNull
    Map<String, Map<GameStatType, Integer>> getExtensionStatistics();

    /**
     * Returns per-extension statistics
     *
     * @param extension Extension to get for
     * @return Per-game mode stats
     */
    @NotNull
    Map<GameStatType, Integer> getExtensionStatistics(@NotNull String extension);

    default Map<GameStatType, Integer> getExtensionStatistics(@NotNull GameExtension extension) {
        Objects.requireNonNull(extension, "extension is null");
        return getExtensionStatistics(extension.getKey());
    }

    /**
     * Returns the player's coins boosters
     *
     * @return The boosters
     */
    @NotNull
    Map<Integer, BoosterInstance> getBoosters();

    /**
     * Returns the key of the selected splegg upgrade
     *
     * @return The selected upgrade
     */
    @NotNull
    String getSelectedSpleggUpgrade();

    /**
     * Returns a map of all perks with their count
     *
     * @return Perks
     */
    @NotNull
    Map<GamePerk, Integer> getPerks();

    /**
     * Returns a list of all splegg upgrade purchases
     *
     * @return All splegg upgrades purchases
     * @see #upgradeKeys()
     */
    @NotNull
    Set<SpleggUpgrade> getPurchasedSpleggUpgrades();

    /**
     * Returns all {@link #getPurchasedSpleggUpgrades()} as a list of strings
     *
     * @return ^
     */
    @NotNull
    Set<String> upgradeKeys();

    /**
     * Returns a list of all active boosters. Note that these boosters are mutable.
     *
     * @return A list of all active boosters.
     */
    @NotNull
    List<BoosterInstance> getActiveBoosters();

    /**
     * Returns a new builder for this data
     *
     * @return A new builder
     */
    @NotNull
    Builder asBuilder();

    /**
     * Creates a new builder
     *
     * @return New builder instance
     */
    static Builder builder(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        return new BuilderImpl(uuid);
    }

    static PlayerProfile blankProfile(@NotNull UUID uuid) {
        return PlayerProfile.builder(uuid)
                .setCoins(50)
                .setSpleggUpgrade("default")
                .resetStats()
                .build();
    }

    /**
     * Builder class for creating instances of {@link PlayerProfile}
     */
    interface Builder {

        /**
         * Sets the amount of coins
         *
         * @param coins New value to set
         * @return This builder instance
         */
        @NotNull
        Builder setCoins(int coins);

        /**
         * Sets the player's coins by applying a function on the original amount.
         *
         * @param modification Function modification
         * @return This builder instance
         */
        @NotNull
        Builder setCoins(IntFunction<Integer> modification);

        /**
         * Adds the specified amount of coins
         *
         * @param coins Value to add
         * @return This builder instance
         */
        @NotNull
        Builder addCoins(int coins);

        /**
         * Removes/takes the specified amount of coins
         *
         * @param coins Value to remove
         * @return This builder instance
         */
        @NotNull
        Builder subtractCoins(int coins);

        /**
         * Returns the amount of coins being maintained by this builder
         *
         * @return The coins
         */
        int coins();

        @NotNull
        Map<GamePerk, Integer> perks();

        /**
         * Sets all statistics to 0
         *
         * @return This builder instance
         */
        @NotNull
        Builder resetStats();

        /**
         * Replaces the specified statistic by applying a function on it
         *
         * @param statType Stat type to replace
         * @param task     Task to modify the value with
         * @return This builder instance
         */
        @NotNull
        Builder replaceStat(@NotNull GameStatType statType, @NotNull IntFunction<Integer> task);

        /**
         * Sets the statistics map from the specified mapping
         *
         * @param stats Map of stats
         * @return This builder instance
         */
        @NotNull
        Builder setStats(@NotNull Map<GameStatType, Integer> stats);

        /**
         * Sets the per-mode statistics map from the specified mapping
         *
         * @param stats Map of stats
         * @return This builder instance
         */
        @NotNull
        Builder setModeStats(@NotNull Map<String, Map<GameStatType, Integer>> stats);

        /**
         * Sets the boosters of this profile
         *
         * @param boosters Boosters map
         * @return This builder instance
         */
        @NotNull
        Builder setBoosters(@NotNull Map<Integer, BoosterInstance> boosters);

        /**
         * Sets the perks map
         *
         * @param perks Perks map
         * @return This builder instance
         */
        @NotNull
        Builder setPerks(@NotNull Map<GamePerk, Integer> perks);

        /**
         * Adds the specified booster
         *
         * @param booster Booster to add
         * @return This builder instance
         */
        @NotNull
        Builder addBooster(@NotNull BoosterInstance booster);

        /**
         * Adds the specified upgrade as a splegg upgrade
         *
         * @param spleggUpgrade Splegg upgrade to add
         * @return This builder instance
         */
        @NotNull
        Builder addPurchase(@NotNull SpleggUpgrade spleggUpgrade);

        /**
         * Replaces the specified statistic by applying a function on it
         *
         * @param statType Stat type to replace
         * @param task     Task to modify the value with
         * @return This builder instance
         */
        @NotNull
        Builder replaceExtensionStat(@NotNull String mode, @NotNull GameStatType statType, @NotNull IntFunction<Integer> task);

        /**
         * Sets the splegg upgrade key
         *
         * @param upgrade Upgrade to set
         * @return This builder instance
         */
        @NotNull
        Builder setSpleggUpgrade(@NotNull String upgrade);

        /**
         * Sets the purchased splegg upgrades
         *
         * @param upgrades A list of upgrades
         * @return This builder instance
         */
        @NotNull
        Builder setPurchasedSpleggUpgrades(@NotNull Set<SpleggUpgrade> upgrades);

        /**
         * Pushes this profile to the data stack, hence applying all changes inside it
         *
         * @return This builder instance
         */
        @NotNull
        Builder push();

        /**
         * Copies all data from the cloned builder into this
         *
         * @param copyFrom Builder to copy from
         * @return This builder instance
         */
        @NotNull
        Builder copy(@NotNull Builder copyFrom);

        /**
         * Creates the player data
         *
         * @return The created data
         */
        @NotNull
        PlayerProfile build();
    }
}