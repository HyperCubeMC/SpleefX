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
package io.github.spleefx.perk;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.github.spleefx.arena.api.ArenaData;
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
import org.moltenjson.adapter.BlockLocationAdapter;
import org.moltenjson.adapter.EnchantmentsAdapter;
import org.moltenjson.adapter.LocationAdapter;
import org.moltenjson.adapter.PotionEffectsAdapter;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.github.spleefx.arena.api.ArenaData.PROJECTILE_ADAPTER;
import static io.github.spleefx.arena.api.ArenaData.TIME_UNIT_ADAPTER;

public class GamePerkAdapter implements JsonSerializer<GamePerk>, JsonDeserializer<GamePerk> {

    // @formatter:off
    private static final RuntimeTypeAdapterFactory<GamePerk> PERK_FACTORY =
            RuntimeTypeAdapterFactory
                    .of(GamePerk.class, "perkInternalId", true)
                    .registerSubtype(AcidicSnowballsPerk.class);

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
            .registerTypeAdapterFactory(PERK_FACTORY)
            .excludeFieldsWithoutExposeAnnotation()
            .serializeNulls()
            .setPrettyPrinting();
    // @formatter:on

    @Override
    public GamePerk deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        GamePerk perk = GSON.registerTypeAdapterFactory(PERK_FACTORY).create().fromJson(jsonElement, type);
        perk.load();
        return perk;
    }

    @Override
    public JsonElement serialize(GamePerk gamePerk, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonElement e = GSON.create().toJsonTree(gamePerk);
        e.getAsJsonObject().addProperty("perkInternalId", gamePerk.getClass().getSimpleName());
        return e;
    }
}
