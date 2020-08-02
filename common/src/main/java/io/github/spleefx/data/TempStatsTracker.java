package io.github.spleefx.data;

import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.data.impl.PlayerProfileImpl.BuilderImpl;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A simple class for tracking all statistics for a player throughout the game and then bulk adding it
 */
public class TempStatsTracker extends BuilderImpl {

    private static final Map<GameStatType, Integer> EMPTY = new HashMap<>();

    public TempStatsTracker(UUID target) {
        super(target);
    }

    public void applyChanges(GameArena arena) {
        PlayerRepository.REPOSITORY.apply(uuid, (oldProfile, builder) -> {
            builder.addCoins(coins);
            for (GameStatType type : GameStatType.values) {
                builder.replaceExtensionStat(arena.getExtension().getKey(), type, i -> i + modeStats.getOrDefault(
                        arena.getExtension().getKey(), EMPTY).getOrDefault(type, 0));
            }
        }).thenAccept(v -> Bukkit.broadcastMessage("Finished. " + v));
    }

    static {
        for (GameStatType type : GameStatType.values)
            EMPTY.put(type, 0);
    }
}