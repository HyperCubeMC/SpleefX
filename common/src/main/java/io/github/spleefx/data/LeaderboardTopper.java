package io.github.spleefx.data;

import io.github.spleefx.data.impl.SimpleLeaderboardTopper;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a player that has a leaderboard position in any context.
 */
public interface LeaderboardTopper {

    /**
     * Returns the UUID of this player
     *
     * @return The UUID
     */
    @NotNull
    UUID getUUID();

    /**
     * Returns the score they have in this context (for example, if context is wins in spleef, then this returns
     * the wins)
     *
     * @return The score
     */
    int getScore();

    /**
     * Returns the OfflinePlayer representation of this topper. Do note that if this is the first
     * time we get this player then it may take some time in order to fetch them from the Mojang API
     *
     * @return The player
     */
    @NotNull
    CompletableFuture<OfflinePlayer> getPlayer();

    /**
     * Creates a new {@link LeaderboardTopper} from the specified UUID and score
     *
     * @param uuid  UUID of the player
     * @param score The score
     * @return The newly created topper instance
     */
    @NotNull
    static LeaderboardTopper of(@NotNull UUID uuid, int score) {
        return new SimpleLeaderboardTopper(uuid, score);
    }

}