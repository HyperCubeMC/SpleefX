package io.github.spleefx.data;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.CacheWriter;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.spleefx.data.impl.PlayerProfileImpl;
import io.github.spleefx.economy.booster.BoosterInstance;
import io.github.spleefx.extension.standard.splegg.SpleggUpgrade;
import io.github.spleefx.perk.GamePerk;
import io.github.spleefx.perk.GamePerk.MapAdapter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.*;

public interface PlayerCacheManager extends CacheLoader<UUID, PlayerProfile>, CacheWriter<UUID, PlayerProfile> {

    Type GLOBAL_STATS_TYPE = new TypeToken<HashMap<GameStatType, Integer>>() {
    }.getType();

    Type UPGRADES_TYPE = new TypeToken<List<SpleggUpgrade>>() {
    }.getType();

    Type EXT_STATS_TYPE = new TypeToken<HashMap<String, Map<GameStatType, Integer>>>() {
    }.getType();

    Type BOOSTERS_TYPE = new TypeToken<HashMap<Integer, BoosterInstance>>() {
    }.getType();

    Type PERKS_TYPE = new TypeToken<HashMap<GamePerk, Integer>>() {
    }.getType();

    /**
     * The GSON used for handling complex columns in tables
     */
    Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeAdapter(PERKS_TYPE, new MapAdapter())
            .registerTypeAdapter(UPGRADES_TYPE, new PlayerProfileImpl.UpgradeAdapter())
            .create();

    /**
     * Caches all the data from the underlying resource. This method will be invoked asynchronously.
     *
     * @param cache Cache to input into
     */
    void cacheAll(LoadingCache<UUID, PlayerProfile> cache);

    /**
     * Saves this cache
     *
     * @param map Map to save
     */
    void writeAll(@NotNull Map<UUID, PlayerProfile> map);

    /**
     * Whether is this cache manager safe to use in asynchronous context.
     *
     * @return Can be used in asynchronous context or not
     */
    default boolean async() {
        return true;
    }

}