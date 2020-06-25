/*
 * * Copyright 2020 github.com/ReflxctionDev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.spleefx.arena;

import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.data.GameStats;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a player who may join an arena
 */
public class ArenaPlayer {

    /**
     * A map of all arena players. Mapped on join, removed on left
     */
    private static final Map<Player, ArenaPlayer> ARENA_PLAYERS = new HashMap<>();

    /**
     * The Bukkit player representative
     */
    private Player player;

    /**
     * The player's state
     */
    private ArenaPlayerState state = ArenaPlayerState.NOT_INGAME;

    /**
     * The player's current arena
     */
    private GameArena currentArena;

    /**
     * Whether is the player spectating or not
     */
    private boolean spectating = false;

    /**
     * Creates a new ArenaPlayer
     *
     * @param player Player to create for
     */
    private ArenaPlayer(Player player) {
        this.player = player;
    }

    /**
     * Returns the Bukkit player instance
     *
     * @return The bukkit player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the player's state
     *
     * @return The state
     */
    public ArenaPlayerState getState() {
        return state;
    }

    public GameStats getStats() {
        return SpleefX.getPlugin().getDataProvider().getStatistics(player);
    }

    public void setSpectating(boolean spectating) {
        this.spectating = spectating;
    }

    public boolean isSpectating() {
        return spectating;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArenaPlayer that = (ArenaPlayer) o;
        return Objects.equals(player.getUniqueId(), that.player.getUniqueId());
    }

    @Override public int hashCode() {
        return Objects.hash(player.getUniqueId());
    }

    /**
     * Returns the current arena
     *
     * @return ^
     */
    public <R extends GameArena> R getCurrentArena() {
        return (R) currentArena;
    }

    /**
     * Sets the player's state
     *
     * @param state New state to set
     * @return This player instance
     */
    public ArenaPlayer setState(ArenaPlayerState state) {
        this.state = state;
        return this;
    }

    /**
     * Sets the current player's arena
     *
     * @param currentArena New arena to set
     * @return This player instance
     */
    public ArenaPlayer setCurrentArena(GameArena currentArena) {
        this.currentArena = currentArena;
        return this;
    }

    /**
     * Returns the {@link ArenaPlayer} instance of the specified player, or maps a new value if the player does not
     * exist.
     *
     * @param player Player to create or retrieve for
     * @return The ArenaPlayer. Never null
     */
    public static ArenaPlayer adapt(Player player) {
        return ARENA_PLAYERS.computeIfAbsent(player, ArenaPlayer::new);
    }

    /**
     * Removes the specified player
     *
     * @param player Player to remove
     * @return The arena player of that player
     */
    public static ArenaPlayer remove(Player player) {
        return ARENA_PLAYERS.remove(player);
    }

    /**
     * Represents the player's current state
     */
    public enum ArenaPlayerState {

        /**
         * The player is not playing a game
         */
        NOT_INGAME,

        /**
         * The player is waiting in a game
         */
        WAITING,

        /**
         * The player is in a game
         */
        IN_GAME,

        /**
         * The player is in a game but spectating
         */
        SPECTATING
    }

}