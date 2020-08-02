package io.github.spleefx.data;

import io.github.spleefx.data.impl.ForwardingCacheManager;
import io.github.spleefx.data.impl.HikariConnector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum StorageType {

    /* Flat files */
    FLAT_FILE("Flat_File"),
    TOML("TOML"),
    JSON("JSON"),

    /* NoSQL */
    MONGODB("MongoDB"),

    /* SQL */
    MYSQL("MySQL"),
    POSTGRESQL("PostgreSQL"),
    H2("H2");

    private final String name;

    StorageType(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Sets the delegating writer and loader to this instance
     */
    public void delegate() {
        StorageMapping mapping = StorageMapping.valueOf(name());
        ForwardingCacheManager.delegateTo(mapping.getCacheManager());
        if (mapping.getCacheManager() instanceof HikariConnector)
            ((HikariConnector) mapping.getCacheManager()).connect();
    }

    private static final Map<String, StorageType> BY_NAME = new HashMap<>();

    @Nullable
    public static StorageType fromName(@NotNull String name) {
        return BY_NAME.get(name.toLowerCase());
    }

    static {
        for (StorageType type : values()) BY_NAME.put(type.name.toLowerCase(), type);
    }

}