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
package org.moltenjson.adapter;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.api.ArenaData;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.arena.bow.BowSpleefArena;
import io.github.spleefx.arena.custom.ExtensionArena;
import io.github.spleefx.arena.spleef.SpleefArena;
import io.github.spleefx.arena.splegg.SpleggArena;
import io.github.spleefx.economy.booster.ActiveBoosterLoader;
import io.github.spleefx.extension.ExtensionTitle;
import io.github.spleefx.extension.GameEvent;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.extension.standard.splegg.SpleggExtension.ProjectileType;
import io.github.spleefx.sign.BlockLocation;
import io.github.spleefx.team.TeamColor;
import io.github.spleefx.util.io.RuntimeTypeAdapterFactory;
import org.bukkit.Location;
import org.bukkit.Material;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.github.spleefx.arena.api.ArenaData.PROJECTILE_ADAPTER;
import static io.github.spleefx.arena.api.ArenaData.TIME_UNIT_ADAPTER;

public class GameArenaAdapter implements JsonSerializer<GameArena>, JsonDeserializer<GameArena> {

    public static final Map<String, JsonElement> REMOVED_ARENAS = new LinkedHashMap<>();

    private static final RuntimeTypeAdapterFactory<GameArena> ARENA_FACTORY =
            RuntimeTypeAdapterFactory.of(GameArena.class, "modeType", true)
                    .registerSubtype(SpleggArena.class)
                    .registerSubtype(SpleefArena.class)
                    .registerSubtype(BowSpleefArena.class)
                    .registerSubtype(ExtensionArena.class);

    // @formatter:off
    public static final GsonBuilder GSON = new GsonBuilder()
            .registerTypeAdapter(TeamColor.class, ArenaData.TEAM_ADAPTER)
            .registerTypeAdapter(ActiveBoosterLoader.MAP_TYPE, ActiveBoosterLoader.ADAPTER)
            .registerTypeAdapter(TimeUnit.class, TIME_UNIT_ADAPTER)
            .registerTypeAdapter(Material.class, ArenaData.MATERIAL_ADAPTER)
            .registerTypeAdapter(Location.class, LocationAdapter.INSTANCE)
            .registerTypeAdapter(ProjectileType.class, PROJECTILE_ADAPTER)
            .registerTypeAdapter(new TypeToken<Map<GameEvent, ExtensionTitle>>() {}.getType(), ArenaData.TITLE_ADAPTER)
            .registerTypeAdapter(BlockLocation.class, BlockLocationAdapter.INSTANCE)
            .registerTypeAdapter(EnchantmentsAdapter.TYPE, EnchantmentsAdapter.INSTANCE)
            .registerTypeAdapter(PotionEffectsAdapter.TYPE, PotionEffectsAdapter.INSTANCE)
            .registerTypeAdapter(GameExtension.class, new GameExtension.StringAdapter())
            .registerTypeAdapterFactory(ARENA_FACTORY)
            .excludeFieldsWithoutExposeAnnotation()
            .serializeNulls()
            .setPrettyPrinting();
    // @formatter:on

    /**
     * Gson invokes this call-back method during deserialization when it encounters a field of the
     * specified type.
     * <p>In the implementation of this call-back method, you should consider invoking
     * {@link JsonDeserializationContext#deserialize(JsonElement, Type)} method to create objects
     * for any non-trivial field of the returned object. However, you should never invoke it on the
     * the same type passing {@code json} since that will cause an infinite loop (Gson will call your
     * call-back method again).
     *
     * @param json    The Json data_old being deserialized
     * @param typeOfT The type of the Object to deserialize to
     * @param context p
     * @return a deserialized object of the specified type typeOfT which is a subclass of {@code T}
     * @throws JsonParseException if json is not in the expected format of {@code typeofT}
     */
    @Override
    public GameArena deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            GameArena arena = GSON.registerTypeAdapterFactory(ARENA_FACTORY).create().fromJson(json, typeOfT);
            arena.post();
            return arena;
        } catch (JsonParseException e) {
            String key = json.getAsJsonObject().get("key").getAsString();
            SpleefX.logger().warning("Failed to load arena \"" + key + "\": " + e.getMessage());
            REMOVED_ARENAS.put(key, json);
            return null;
        }
    }

    /**
     * Gson invokes this call-back method during serialization when it encounters a field of the
     * specified type.
     *
     * <p>In the implementation of this call-back method, you should consider invoking
     * {@link JsonSerializationContext#serialize(Object, Type)} method to create JsonElements for any
     * non-trivial field of the {@code src} object. However, you should never invoke it on the
     * {@code src} object itself since that will cause an infinite loop (Gson will call your
     * call-back method again).</p>
     *
     * @param src       the object that needs to be converted to Json.
     * @param typeOfSrc the actual type (fully genericized version) of the source object.
     * @param context
     * @return a JsonElement corresponding to the specified object.
     */
    @Override
    public JsonElement serialize(GameArena src, Type typeOfSrc, JsonSerializationContext context) {
        JsonElement e = GSON.create().toJsonTree(src);
        e.getAsJsonObject().addProperty("modeType", src.getClass().getSimpleName());
        return e;
    }

    public static <R extends GameArena> void registerArena(Class<R> arenaClass) {
        ARENA_FACTORY.registerSubtype(arenaClass);
    }

}