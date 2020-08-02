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
package io.github.spleefx.arena.api;

import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.ArenaStage;
import io.github.spleefx.extension.GameExtension.ScoreboardType;
import io.github.spleefx.extension.ability.GameAbility;
import io.github.spleefx.sign.SignManager;
import io.github.spleefx.team.GameTeam;
import io.github.spleefx.util.message.message.Message;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Class which controls all the arena processing
 */
public interface ArenaEngine {

    /**
     * Returns the current game stage
     *
     * @return The current game stage
     */
    ArenaStage getArenaStage();

    /**
     * Updates the game stage and all the subordinate signs
     *
     * @param stage New stage to set
     */
    void setArenaStage(ArenaStage stage);

    /**
     * Selects a team randomly as long as it's not full
     *
     * @return The randomly selected team
     */
    GameTeam selectTeam();

    /**
     * Joins the player to the arena
     *
     * @param player Player to join
     */
    boolean join(ArenaPlayer player, GameTeam team);

    /**
     * Invoked when the player quits
     *
     * @param player Player to quit
     */
    void quit(ArenaPlayer player, boolean disconnect);

    /**
     * Invoked when the player loses
     *
     * @param player Player to lose
     * @param team   The player's team
     */
    void lose(ArenaPlayer player, GameTeam team, boolean disconnect);

    /**
     * Invoked when the player wins
     *
     * @param player Player to win
     * @param team   The player's team
     */
    void win(ArenaPlayer player, GameTeam team);

    /**
     * Invoked when the game result is draw
     */
    void draw();

    /**
     * Prepares the player to the game, by adding effects
     *
     * @param player Player to prepare
     * @param team   The player's team
     */
    void prepare(ArenaPlayer player, GameTeam team);

    /**
     * Prepares the player to the game, by teleporting, adding items, etc
     *
     * @param player Player to prepare
     * @param team   The player's team
     */
    void prepareForGame(ArenaPlayer player, GameTeam team);

    /**
     * Runs the countdown
     */
    void countdown();

    /**
     * Starts the game
     */
    void start();

    /**
     * Runs the game loop
     */
    void loop();

    /**
     * Ends the game
     *
     * @param giveRewards Whether should the engine give rewards to the winners
     */
    void end(boolean giveRewards);

    /**
     * Forcibly ends the game (used when the server is shutting down)
     */
    void forceEnd();

    /**
     * Regenerates the arena
     * @param newStage
     */
    void regenerate(@Nullable ArenaStage newStage);

    /**
     * Saves the player data_old before they enter the arena, such as the inventory and location
     *
     * @param player Player to save for
     */
    void save(ArenaPlayer player);

    /**
     * Loads the saved data_old into the player
     *
     * @param player Player to load for
     */
    void load(ArenaPlayer player, boolean delayTick);

    /**
     * Displays the game scoreboard for the player
     *
     * @param player Player to display for
     */
    void displayScoreboard(ArenaPlayer player);

    /**
     * Sends a message to all the players
     */
    void broadcast(Message message, Object... format);

    /**
     * Returns the signs manager, which updates all signs accordingly
     *
     * @return The signs manager
     */
    SignManager getSignManager();

    /**
     * Returns a map of all arena players assigned to their teams
     *
     * @return ^
     */
    Map<ArenaPlayer, GameTeam> getPlayerTeams();

    /**
     * Returns the ability count of each player
     *
     * @return The ability count map
     */
    Map<UUID, EnumMap<GameAbility, Integer>> getAbilityCount();

    /**
     * Returns whether is this arena full or not
     *
     * @return ^
     */
    boolean isFull();

    ScoreboardType getCurrentScoreboard();

    List<Player> getAlive();

    List<GameTeam> getDeadTeams();
}