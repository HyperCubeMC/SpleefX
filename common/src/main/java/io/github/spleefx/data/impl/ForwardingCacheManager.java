package io.github.spleefx.data.impl;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.CacheWriter;
import com.github.benmanes.caffeine.cache.RemovalCause;
import io.github.spleefx.data.PlayerCacheManager;
import io.github.spleefx.data.PlayerProfile;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ForwardingCacheManager implements CacheWriter<UUID, PlayerProfile>, CacheLoader<UUID, PlayerProfile> {

    private static PlayerCacheManager delegate;
    private static final Object MUTEX = new Object();

    public static void delegateTo(PlayerCacheManager delegate) {
        synchronized (MUTEX) {
            ForwardingCacheManager.delegate = delegate;
        }
    }

    @Override
    public void write(@NonNull UUID key, @NonNull PlayerProfile value) {
        Objects.requireNonNull(delegate, "Writer").write(key, value);
    }

    @Override
    public void delete(@NonNull UUID key, @Nullable PlayerProfile value, @NonNull RemovalCause cause) {
        Objects.requireNonNull(delegate, "Writer").delete(key, value, cause);
    }

    @Nullable
    @Override
    public PlayerProfile load(@NonNull UUID key) throws Exception {
        return delegate.load(key);
    }

    @Override
    public @NonNull Map<UUID, PlayerProfile> loadAll(@NonNull Iterable<? extends UUID> keys) throws Exception {
        return delegate.loadAll(keys);
    }

    @Nullable @Override public PlayerProfile reload(@NonNull UUID key, @NonNull PlayerProfile oldValue) throws Exception {
        return delegate.reload(key, oldValue);
    }

    @NotNull
    public static PlayerCacheManager delegate() {
        return delegate;
    }
}