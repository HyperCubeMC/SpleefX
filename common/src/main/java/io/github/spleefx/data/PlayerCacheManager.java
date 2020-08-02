package io.github.spleefx.data;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.CacheWriter;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public interface PlayerCacheManager extends CacheLoader<UUID, PlayerProfile>, CacheWriter<UUID, PlayerProfile> {

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