package io.github.spleefx.data.impl;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.spleefx.SpleefX;
import io.github.spleefx.data.*;
import io.github.spleefx.data.PlayerProfile.Builder;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static java.util.Collections.reverseOrder;
import static java.util.Objects.requireNonNull;

public class PlayerRepositoryImpl implements PlayerRepository {

    private final ForwardingCacheManager forwardingCacheManager = new ForwardingCacheManager();

    private final LoadingCache<UUID, PlayerProfile> cache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofHours(6))
            .maximumSize(1_000)
            .executor(SpleefX.POOL)
            .writer(forwardingCacheManager)
            .build(forwardingCacheManager);

    private final Map<GameStatType, Set<LeaderboardTopper>> top = new HashMap<>();
    private final Map<GameStatType, Map<String, Set<LeaderboardTopper>>> topByExtension = new HashMap<>();

    @Override
    public void insert(@NotNull UUID uuid, @NotNull PlayerProfile data) {
        cache.put(uuid, data);
    }

    @Override
    public List<LeaderboardTopper> getTopPlayers(@NotNull GameStatType stat, @Nullable GameExtension extension) {
        requireNonNull(stat, "stat");
        if (extension == null)
            return new ArrayList<>(top.get(stat));
        return new ArrayList<>(topByExtension.get(stat).get(extension.getKey()));
    }

    @Override
    public @Nullable PlayerProfile lookup(@NotNull UUID uuid) {
        return cache.getIfPresent(uuid);
    }

    @Override
    public @NotNull CompletableFuture<PlayerProfile> getOrQuery(@NotNull UUID uuid) {
        CompletableFuture<PlayerProfile> future = new CompletableFuture<>();
        Runnable task = () -> {
            PlayerProfile profile = cache.get(uuid);
            future.complete(profile);
        };
        if (ForwardingCacheManager.delegate().async()) {
            SpleefX.POOL.submit(task);
        } else {
            task.run();
        }
        return future;
    }

    @Override public @NotNull CompletableFuture<PlayerProfile> apply(@NotNull UUID uuid, @NotNull BiConsumer<PlayerProfile, Builder> modification) {
        CompletableFuture<PlayerProfile> f = new CompletableFuture<>();
        Runnable task = () -> {
            try {
                cache.asMap().computeIfPresent(uuid, (k, v) -> {
                    try {
                        Builder builder = v.asBuilder();
                        modification.accept(v, builder);
                        PlayerProfile profile = builder.build();
                        f.complete(profile);
                        return profile;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                });
                f.complete(null);
            } catch (Exception e) {
                e.printStackTrace();
                f.complete(null);
            }
        };
        if (ForwardingCacheManager.delegate().async()) {
            SpleefX.POOL.submit(task);
        } else {
            task.run();
        }
        return f;
    }

    @Override public void cacheAll() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (ForwardingCacheManager.delegate().async()) {
            SpleefX.POOL.submit(() -> {
                ForwardingCacheManager.delegate().cacheAll(cache);
                future.complete(null);
            });
        } else {
            ForwardingCacheManager.delegate().cacheAll(cache);
            future.complete(null);
        }
        future.thenAccept(v -> Bukkit.getScheduler().runTaskTimer(SpleefX.getPlugin(), this::sortLeaderboards, 0, 600 * 20));
    }

    private void sortLeaderboards() {
        CompletableFuture<Map<GameStatType, Set<LeaderboardTopper>>> topFuture = new CompletableFuture<>();
        CompletableFuture<Map<GameStatType, Map<String, Set<LeaderboardTopper>>>> topByExtFuture = new CompletableFuture<>();

        SpleefX.POOL.submit(() -> {
            Map<GameStatType, Map<String, Set<LeaderboardTopper>>> topByExt = new HashMap<>();
            Map<GameStatType, Set<LeaderboardTopper>> top = new HashMap<>();

            for (GameStatType stat : GameStatType.values) {
                List<Entry<UUID, PlayerProfile>> profiles = new ArrayList<>(cache.asMap().entrySet());
                profiles.sort(reverseOrder(Comparator.comparingInt(e -> e.getValue().getGameStats().get(stat))));
                Set<LeaderboardTopper> toppers = profiles.stream().map(player -> LeaderboardTopper.of(player.getKey(),
                        player.getValue().getGameStats().get(stat))).collect(Collectors.toCollection(LinkedHashSet::new));
                top.put(stat, toppers);

                Map<String, Set<LeaderboardTopper>> tops = new HashMap<>();
                for (GameExtension extension : ExtensionsManager.EXTENSIONS.values()) {
                    List<LeaderboardTopper> topEx = cache.asMap().entrySet().stream()
                            .sorted(reverseOrder(Comparator.comparingInt(e -> e.getValue().getExtensionStatistics().get(extension.getKey()).get(stat))))
                            .map(e -> LeaderboardTopper.of(e.getKey(), e.getValue().getExtensionStatistics().get(extension.getKey()).get(stat)))
                            .collect(Collectors.toList());
                    tops.put(extension.getKey(), new LinkedHashSet<>(topEx));
                }
                topByExt.put(stat, tops);
                topByExtFuture.complete(topByExt);
                topFuture.complete(top);
            }
        });

        topFuture.thenAcceptAsync(t -> {
            synchronized (top) {
                top.clear();
                top.putAll(t);
            }
        });
        topByExtFuture.thenAcceptAsync(t -> {
            synchronized (topByExtension) {
                topByExtension.clear();
                topByExtension.putAll(t);
            }
        });
    }

    public Map<GameStatType, Set<LeaderboardTopper>> getTop() {
        synchronized (top) {
            return top;
        }
    }

    public Map<GameStatType, Map<String, Set<LeaderboardTopper>>> getTopByExtension() {
        synchronized (topByExtension) {
            return topByExtension;
        }
    }

    @Override public void save() {
        ForwardingCacheManager.delegate().writeAll(cache.asMap());
    }

    static {
        StorageType.H2.delegate();
    }
}