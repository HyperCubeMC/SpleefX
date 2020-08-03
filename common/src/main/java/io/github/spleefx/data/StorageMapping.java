package io.github.spleefx.data;

import io.github.spleefx.data.database.sql.SQLBasedManager;
import org.jetbrains.annotations.NotNull;

public enum StorageMapping {

    MONGODB(null),
    MYSQL(SQLBasedManager.MYSQL),
    POSTGRESQL(SQLBasedManager.POSTGRESQL),
    H2(SQLBasedManager.H2);

    private final PlayerCacheManager cacheManager;

    StorageMapping(@NotNull PlayerCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public PlayerCacheManager getCacheManager() {
        return cacheManager;
    }

}