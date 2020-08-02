package io.github.spleefx.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;

@ToString
public class ConfigOption<V> {

    @Nullable
    private final String path;

    @Nullable
    private final V def;

    @NotNull
    @Exclude
    private final ValueFactory<V> factory;

    private V value;
    protected boolean reloadable = false;

    public ConfigOption(@Nullable String path, @Nullable V def, @NotNull ValueFactory<V> factory) {
        this.path = path;
        this.def = def;
        this.factory = Objects.requireNonNull(factory, "factory is null");
    }

    public void load(FileConfiguration config, boolean initial) {
        if (initial || reloadable)
            value = factory.resolve(config, path, def);
    }

    public V get() {
        return value;
    }

    /**
     * Loads all settings
     *
     * @param options       Options to load.
     * @param configuration Configuration to load from
     * @param initial       Whether is this the first time loading the settings.
     */
    public static void load(@NotNull List<ConfigOption<?>> options, @NotNull FileConfiguration configuration, boolean initial) {
        options.forEach(c -> c.load(configuration, initial));
    }

    public static List<ConfigOption<?>> locateSettings(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(f -> ConfigOption.class.isAssignableFrom(f.getType()))
                .map(f -> {
                    try {
                        return (ConfigOption<?>) f.get(null);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(toImmutableList());
    }

    private static final Collector<Object, Builder<Object>, ImmutableList<Object>> IMMUTABLE_LIST_COLLECTOR = Collector.of(
            Builder::new,
            Builder::add,
            (l, r) -> l.addAll(r.build()),
            Builder::build
    );

    @SuppressWarnings("all")
    public static <T> Collector<T, Builder<T>, ImmutableList<T>> toImmutableList() {
        return (Collector) IMMUTABLE_LIST_COLLECTOR;
    }
}