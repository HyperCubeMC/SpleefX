package io.github.spleefx.data;

import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.data.PlayerProfile.Builder;
import io.github.spleefx.data.impl.PlayerRepositoryImpl;
import io.github.spleefx.extension.GameExtension;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * Represents a repository which contains multiple profiles
 *
 * @see PlayerProfile
 */
public interface PlayerRepository {

    PlayerRepository REPOSITORY = PlayerRepository.create();

    /**
     * Inserts the specified player data
     *
     * @param uuid UUID of the player
     * @param data The player's data
     */
    void insert(@NotNull UUID uuid, @NotNull PlayerProfile data);

    List<LeaderboardTopper> getTopPlayers(@NotNull GameStatType stat, @Nullable GameExtension extension);

    /**
     * Looks up a {@link PlayerProfile} from cache
     *
     * @param uuid UUID of the player to retrieve
     * @return The player profile, or null if not cached.
     */
    /*@Nullable*/
    PlayerProfile lookup(@NotNull UUID uuid);

    /**
     * Looks up a {@link PlayerProfile} from cache
     *
     * @param player Player to retrieve for
     * @return The player profile, or null if not cached.
     */
    /*@Nullable*/
    default PlayerProfile lookup(@NotNull OfflinePlayer player) {
        return lookup(player.getUniqueId());
    }

    /**
     * Looks up a {@link PlayerProfile} from cache
     *
     * @param player Player to retrieve for
     * @return The player profile, or null if not cached.
     */
    /*@Nullable*/
    default PlayerProfile lookup(@NotNull ArenaPlayer player) {
        return lookup(player.getPlayer().getUniqueId());
    }

    /**
     * Returns the data of the UUID from cache, or loads it directly if not
     * present.
     *
     * @param uuid The player UUID
     * @return The data result, or null if none.
     */
    @NotNull
    CompletableFuture<PlayerProfile> getOrQuery(@NotNull UUID uuid);

    /**
     * Applies a modification to the specified UUID's entry
     *
     * @param uuid         UUID to modify
     * @param modification Changes to run
     * @return A future indicating the progress of this modification
     */
    @NotNull
    CompletableFuture<PlayerProfile> apply(@NotNull UUID uuid, @NotNull BiConsumer<PlayerProfile, Builder> modification);

    /**
     * Builds the full cache
     */
    void cacheAll();

    /**
     * Saves all the data
     */
    void save();

    /**
     * Creates a new PlayerRepository instance
     *
     * @return The new player repository
     */
    @NotNull
    static PlayerRepository create() {
        return new PlayerRepositoryImpl();
    }

    class QueryListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void onPlayerJoin(PlayerJoinEvent event) {
            REPOSITORY.getOrQuery(event.getPlayer().getUniqueId())
                    .thenAccept(profile -> {
                        if (profile == null)
                            REPOSITORY.insert(event.getPlayer().getUniqueId(), PlayerProfile.blankProfile(event.getPlayer().getUniqueId()));
                    });
        }
    }

}