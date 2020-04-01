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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import io.github.spleefx.arena.ArenaStage;
import io.github.spleefx.arena.ModeType;
import io.github.spleefx.arena.SimpleArenaEngine;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.team.GameTeam;
import io.github.spleefx.team.TeamColor;
import org.bukkit.Location;
import org.moltenjson.adapter.GameArenaAdapter;
import org.moltenjson.configuration.select.SelectKey;
import org.moltenjson.configuration.select.SelectionHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a basic game arena.
 */
@JsonAdapter(GameArenaAdapter.class)
public class GameArena extends ArenaData {

    /**
     * A map that contains all arenas assigned to their keys
     */
    @SelectKey("arenas")
    public static final SelectionHolder<Map<String, GameArena>> ARENAS = new SelectionHolder<>(new HashMap<>(), false);

    /**
     * Represents the arena type/id. Derived from {@link Class#getSimpleName()}.
     * <p>
     * For example, a splegg arean would be {@code SpleggArena}.
     */
    @Expose
    protected String modeType;

    /**
     * Represents the mode type
     */
    @Expose
    public ModeType type;

    /**
     * Represents the arena's ffa manager
     */
    @Expose
    @SerializedName("ffaSettings")
    private FFAManager ffaManager;

    /**
     * A list of all teams
     */
    public List<GameTeam> gameTeams;

    /**
     * The arena's engine
     */
    private ArenaEngine engine;

    /**
     * The amount of bets that should be taken from everyone when the game starts
     */
    @Expose
    private int bet = 1;

    /**
     * Whether should the arena take bets when starting
     */
    @Expose
    private boolean takeBets = false;

    /**
     * The arena's extension mode
     */
    private GameExtension extension = new GameExtension();

    /**
     * Creates a new spleef arena
     *
     * @param key               Key of the arena
     * @param displayName       Display name of the arena
     * @param regenerationPoint The regeneration point of the arena
     */
    public GameArena(String key, String displayName, Location regenerationPoint, ArenaType type) {
        super(key, displayName, regenerationPoint, type);
        post();
    }

    public void linkTeams() {
        getTeams().forEach(team -> gameTeams.add(new GameTeam(team, new ArrayList<>())));
    }

    /**
     * Getter for property 'gameTeams'.
     *
     * @return Value for property 'gameTeams'.
     */
    public List<GameTeam> getGameTeams() {
        return gameTeams;
    }

    /**
     * Sets the engine instance
     *
     * @param arenaEngine Engine to set
     */
    protected void setEngine(ArenaEngine arenaEngine) {
        engine = arenaEngine;
    }

    /**
     * Returns the engine instance
     *
     * @return The engine
     */
    public ArenaEngine getEngine() {
        return engine;
    }

    public FFAManager getFFAManager() {
        return ffaManager;
    }

    /**
     * Returns the arena's extension mode
     */
    public GameExtension getExtension() {
        return extension;
    }

    /**
     * Sets the arena's extension
     *
     * @param extension New extension to set
     */
    protected void setExtension(GameExtension extension) {
        this.extension = extension;
    }

    public void setFFAManager(FFAManager ffaManager) {
        this.ffaManager = ffaManager;
    }

    public int getBet() {
        return bet < 1 ? bet = 1 : bet;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }

    public boolean shouldTakeBets() {
        return bet > 1 && takeBets;
    }

    public void setTakeBets(boolean takeBets) {
        this.takeBets = takeBets;
    }

    @Override
    public void setArenaType(ArenaType arenaType) {
        super.setArenaType(arenaType);
        if (arenaType == ArenaType.FREE_FOR_ALL) {
            getTeams().add(TeamColor.FFA);
            getGameTeams().add(new GameTeam(TeamColor.FFA, new ArrayList<>()));
        }
    }

    public void post() {
        this.modeType = getClass().getSimpleName();
        this.stage = ArenaStage.WAITING;
        this.engine = new SimpleArenaEngine<>(this);
        this.gameTeams = new ArrayList<>();
        ARENAS.get().put(getKey(), this);
        linkTeams();
    }

    /**
     * Returns an arena by its key
     *
     * @param key Key to retrieve from
     * @return The arena
     */
    public static GameArena getByKey(String key) {
        return ARENAS.get().get(key);
    }

    /**
     * Finds an arena by its display name
     *
     * @param name Display name to look up
     * @return The arena, or null if not found
     */
    public static GameArena getByName(String name) {
        return ARENAS.get().values().stream().filter(e -> e.getDisplayName().equals(name)).findFirst().orElse(null);
    }

}
