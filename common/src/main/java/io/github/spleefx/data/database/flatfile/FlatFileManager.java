package io.github.spleefx.data.database.flatfile;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import io.github.spleefx.data.PlayerCacheManager;
import io.github.spleefx.data.PlayerProfile;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class FlatFileManager implements PlayerCacheManager {

    @Nullable @Override public PlayerProfile load(@NonNull UUID key) {
        return null;
    }

    @Override public void cacheAll(LoadingCache<UUID, PlayerProfile> cache) {

    }

    @Override public void writeAll(@NotNull Map<UUID, PlayerProfile> map) {

    }

    @Override public void write(@NonNull UUID key, @NonNull PlayerProfile value) {

    }

    @Override public void delete(@NonNull UUID key, @Nullable PlayerProfile value, @NonNull RemovalCause cause) {

    }
}
