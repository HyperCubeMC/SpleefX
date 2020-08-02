package io.github.spleefx.data.impl;

import io.github.spleefx.data.LeaderboardTopper;
import io.github.spleefx.data.OfflinePlayerFactory;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SimpleLeaderboardTopper implements LeaderboardTopper {

    private final UUID player;
    private final int score;

    private OfflinePlayer offlinePlayer;

    public SimpleLeaderboardTopper(UUID player, int score) {
        this.player = Objects.requireNonNull(player);
        this.score = score;
    }

    @NotNull
    @Override
    public UUID getUUID() {
        return player;
    }

    @Override
    public int getScore() {
        return score;
    }

    @NotNull
    @Override
    public CompletableFuture<OfflinePlayer> getPlayer() {
        synchronized (player) {
            return offlinePlayer == null ? OfflinePlayerFactory.FACTORY.getOrRequest(player).thenApply(p -> offlinePlayer = p) : CompletableFuture.completedFuture(offlinePlayer);
        }
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleLeaderboardTopper that = (SimpleLeaderboardTopper) o;
        return Objects.equals(player, that.player);
    }

    @Override public int hashCode() {
        return Objects.hash(player);
    }

    @Override public String toString() {
        return "SimpleLeaderboardTopper{" + "player=" + player +
                ", score=" + score +
                ", offlinePlayer=" + offlinePlayer +
                '}';
    }
}