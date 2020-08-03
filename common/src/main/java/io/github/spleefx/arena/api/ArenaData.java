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

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.ArenaStage;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.economy.booster.ActiveBoosterLoader;
import io.github.spleefx.extension.ExtensionTitle;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameEvent;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.extension.standard.splegg.SpleggExtension.ProjectileType;
import io.github.spleefx.perk.GamePerk;
import io.github.spleefx.perk.GamePerkAdapter;
import io.github.spleefx.sign.BlockLocation;
import io.github.spleefx.team.TeamColor;
import io.github.spleefx.util.Percentage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.moltenjson.adapter.*;
import org.moltenjson.json.JsonBuilder;
import org.moltenjson.utils.AdapterBuilder;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A backing class for providing arena data. This serves no special compatibility purpose other than separating
 * common arena settings from type-special arena data.
 */
@Getter
@Setter
public abstract class ArenaData {

    public static final AdapterBuilder<TeamColor> TEAM_ADAPTER = new AdapterBuilder<TeamColor>()
            .serialization(color -> new JsonPrimitive(color.getName()))
            .deserialization(element -> TeamColor.get(element.getAsString()));

    public static final AdapterBuilder<TimeUnit> TIME_UNIT_ADAPTER = new AdapterBuilder<TimeUnit>()
            .serialization(unit -> new JsonPrimitive(unit.toString()))
            .deserialization(element -> {
                String e = element.getAsString().toUpperCase();
                return TimeUnit.valueOf(e.endsWith("S") ? e : e + "S");
            });

    public static final AdapterBuilder<Material> MATERIAL_ADAPTER = new AdapterBuilder<Material>()
            .serialization(material -> new JsonPrimitive(material.name().toLowerCase()))
            .deserialization(element -> Material.matchMaterial(element.getAsString().toUpperCase()));

    public static final AdapterBuilder<ProjectileType> PROJECTILE_ADAPTER = new AdapterBuilder<ProjectileType>()
            .serialization(projectile -> new JsonPrimitive(projectile.name().toLowerCase()))
            .deserialization(element -> ProjectileType.valueOf(element.getAsString().toUpperCase()));

    public static final AdapterBuilder<Map<GameEvent, ExtensionTitle>> TITLE_ADAPTER = new AdapterBuilder<Map<GameEvent, ExtensionTitle>>()
            .serialization(map -> {
                JsonBuilder builder = new JsonBuilder();
                map.forEach((event, title) -> builder.map(event.name().toLowerCase(), title));
                return builder.buildJsonElement();
            })
            .deserialization(element -> element.getAsJsonObject().entrySet().stream().collect(Collectors.toMap(entry -> GameEvent.valueOf(entry.getKey().toUpperCase()), entry -> AdapterBuilder.standard(entry.getValue(), ExtensionTitle.class), (a, b) -> b)));

    private static final JsonDeserializer<GameExtension> EXTENSION_ADAPTER = (json, typeOfT, context) -> {
        GameExtension mode = null;
        JsonObject o = json.getAsJsonObject();
        try {
            mode = AdapterBuilder.standard(json, typeOfT);
            if (ExtensionsManager.mapExtension(mode.getKey(), mode))
                SpleefX.logger().info("Extension \"" + mode.getKey() + "\" successfully loaded!");
        } catch (Exception e) {
            SpleefX.logger().severe("Failed to load extension \"" + o.get("key").getAsString() + "\".");
            SpleefX.logger().severe("If the error below is a JsonSyntaxException, then it's most likely a problem in your JSON syntax. If you're unable to spot the problem, or if it's not a JsonSyntaxException,");
            SpleefX.logger().severe("then drop by the Discord (on SpigotMC page) and ask for support. MAKE SURE TO INCLUDE THE ERROR BELOW!");
            e.printStackTrace();
            CompatibilityHandler.disable();
            Bukkit.getPluginManager().disablePlugin(SpleefX.getPlugin());

        }
        return mode;
    };

    // @formatter:off
    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeAdapter(TeamColor.class, TEAM_ADAPTER)
            .registerTypeAdapter(Percentage.class, new Percentage.Adapter())
            .registerTypeAdapter(ActiveBoosterLoader.MAP_TYPE, ActiveBoosterLoader.ADAPTER)
            .registerTypeAdapter(TimeUnit.class, TIME_UNIT_ADAPTER)
            .registerTypeAdapter(Material.class, MATERIAL_ADAPTER)
            .registerTypeAdapter(ProjectileType.class, PROJECTILE_ADAPTER)
            .registerTypeAdapter(Location.class, LocationAdapter.INSTANCE)
            .registerTypeAdapter(BlockLocation.class, BlockLocationAdapter.INSTANCE)
            .registerTypeAdapter(GameArena.class, new GameArenaAdapter())
            .registerTypeAdapter(GamePerk.class, new GamePerkAdapter())
            .registerTypeAdapter(EnchantmentsAdapter.TYPE, EnchantmentsAdapter.INSTANCE)
            .registerTypeAdapter(PotionEffectsAdapter.TYPE, PotionEffectsAdapter.INSTANCE)
            .registerTypeAdapter(GameExtension.class, EXTENSION_ADAPTER)
            .registerTypeAdapter(new TypeToken<Map<GameEvent, ExtensionTitle>>() {}.getType(), TITLE_ADAPTER)
            .serializeNulls()
            .excludeFieldsWithoutExposeAnnotation()
            .setPrettyPrinting()
            .create();
    // @formatter:on

    /**
     * Whether is the arena joinable or not.
     */
    @Expose
    private boolean enabled = true;
    /**
     * The arena key. Should be unique for every arena
     */
    @Expose
    private String key;

    /**
     * The arena display name (signs, chat, etc.)
     */
    @Expose
    private String displayName;

    ArenaStage stage = ArenaStage.WAITING;

    /**
     * The arena type
     */
    @Expose
    private ArenaType arenaType;

    /**
     * The location in which the arena will be pasted at whenever it is regenerated
     */
    @Expose
    private Location regenerationPoint;

    /**
     * The finishing location that players are teleported to when the game is over
     */
    @Expose
    @Setter(AccessLevel.NONE)
    private Location finishingLocation;

    /**
     * All signs that redirect to this arena
     */
    @Expose
    private List<BlockLocation> signs = new ArrayList<>();

    /**
     * All team colors that play in this arena
     */
    @Expose
    private Set<TeamColor> teams = new HashSet<>();

    /**
     * Represents the map spawnpoints
     */
    @Expose
    private Map<TeamColor, Location> spawnPoints = new HashMap<>();

    /**
     * Count of members in each team
     */
    @Expose
    private int membersPerTeam = 1;

    /**
     * The time a game is allowed to remain, otherwise would be draw (in minutes)
     */
    @Expose
    private int gameTime = 1;

    /**
     * The Y level in which a player dies
     */
    @Expose
    private int deathLevel = 1;

    /**
     * The minimum amount of players required to start the game
     */
    @Expose
    private int minimum = 2;

    /**
     * Only used if the arena is FFA
     */
    @Expose
    private int maxPlayerCount = -1;

    /**
     * Whether should mined blocks be dropped
     */
    @Expose
    private boolean dropMinedBlocks;

    /**
     * The arena's lobby, where all the waiting players are teleported
     */
    @Expose
    @Setter(AccessLevel.NONE)
    private Location lobby;

    @Expose
    private Map<TeamColor, Location> teamLobbies = new HashMap<>();

    ArenaData(String key, String displayName, Location regenerationPoint, ArenaType arenaType) {
        this.key = key;
        this.displayName = displayName;
        this.regenerationPoint = regenerationPoint;
        ((GameArena) this).gameTeams = new CopyOnWriteArrayList<>();
        if (arenaType == ArenaType.FREE_FOR_ALL)
            ((GameArena) this).setFFAManager(new FFAManager());
        setArenaType(arenaType);
    }

    /**
     * Registers a spawn point for the team
     *
     * @param color    Color of the team
     * @param location Location of the spawnpoint
     */
    public void registerSpawnPoint(TeamColor color, Location location) {
        spawnPoints.put(color, location);
    }

    public int getDeathLevel() {
        return deathLevel < 1 ? deathLevel = 1 : deathLevel;
    }

    public int getMaximum() {
        return arenaType == ArenaType.FREE_FOR_ALL ? getMaxPlayerCount() : getMembersPerTeam() * getTeams().size();
    }

    public Location setLobby(Location loc) {
        lobby = loc;
        return loc;
    }

    public Location setFinishingLocation(Location loc) {
        finishingLocation = loc;
        return loc;
    }

    /**
     * Sets the lines of the signs
     *
     * @param lines New lines to set. Null values do not change
     */
    public void lines(String... lines) {
        for (Iterator<BlockLocation> iterator = getSigns().iterator(); iterator.hasNext(); ) {
            BlockLocation loc = iterator.next();
            try {
                Sign sign = (Sign) loc.world().getBlockAt(loc.getX(), loc.getY(), loc.getZ()).getState();
                IntStream.range(0, lines.length).filter(i -> lines[i] != null).forEachOrdered(i -> sign.setLine(i, lines[i]));
                sign.update();
            } catch (ClassCastException e) { // The sign no longer exists
                iterator.remove();
                SpleefX.logger().info("An arena sign at (" + loc + ") no longer exists. Removing it");
            }
        }
    }

    public Map<TeamColor, Location> getTeamLobbies() {
        return teamLobbies == null ? teamLobbies = new HashMap<>() : teamLobbies;
    }
}