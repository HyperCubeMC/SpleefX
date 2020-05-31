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
package org.moltenjson.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.github.spleefx.arena.api.ArenaData;
import io.github.spleefx.economy.booster.ActiveBoosterLoader;
import io.github.spleefx.extension.ExtensionTitle;
import io.github.spleefx.extension.GameEvent;
import io.github.spleefx.extension.standard.splegg.SpleggExtension.ProjectileType;
import io.github.spleefx.sign.BlockLocation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.moltenjson.adapter.BlockLocationAdapter;
import org.moltenjson.adapter.EnchantmentsAdapter;
import org.moltenjson.adapter.LocationAdapter;
import org.moltenjson.adapter.PotionEffectsAdapter;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static io.github.spleefx.arena.api.ArenaData.PROJECTILE_ADAPTER;

/**
 * A simple provider creator, not meant for advanced usage
 *
 * @param <R> Element to handle serializing and deserializing for
 */
public class AdapterBuilder<R> implements JsonSerializer<R>, JsonDeserializer<R> {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Material.class, ArenaData.MATERIAL_ADAPTER)
            .registerTypeAdapter(TimeUnit.class, ArenaData.TIME_UNIT_ADAPTER)
            .registerTypeAdapter(ActiveBoosterLoader.MAP_TYPE, ActiveBoosterLoader.ADAPTER)
            .registerTypeAdapter(Location.class, LocationAdapter.INSTANCE)
            .registerTypeAdapter(ProjectileType.class, PROJECTILE_ADAPTER)
            .registerTypeAdapter(new TypeToken<Map<GameEvent, ExtensionTitle>>() {
            }.getType(), ArenaData.TITLE_ADAPTER)
            .registerTypeAdapter(BlockLocation.class, BlockLocationAdapter.INSTANCE)
            .registerTypeAdapter(EnchantmentsAdapter.TYPE, EnchantmentsAdapter.INSTANCE)
            .registerTypeAdapter(PotionEffectsAdapter.TYPE, PotionEffectsAdapter.INSTANCE)
            .excludeFieldsWithoutExposeAnnotation()
            .serializeNulls()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    // @formatter:on

    /**
     * The Gson-standard serialization behavior
     */
    private static final Function<Object, JsonElement> STANDARD_SERIALIZATION = AdapterBuilder::standard;

    /**
     * The serialization strategy
     */
    private Function<R, JsonElement> serialization;

    /**
     * The deserialization strategy
     */
    private Function<JsonElement, R> deserialization;

    /**
     * Sets the serialization strategy.
     *
     * @param serialization Serialization strategy
     */
    public AdapterBuilder<R> serialization(Function<R, JsonElement> serialization) {
        this.serialization = serialization;
        return this;
    }

    /**
     * Sets the deserialization strategy.
     *
     * @param deserialization Serialization strategy
     */
    public AdapterBuilder<R> deserialization(Function<JsonElement, R> deserialization) {
        this.deserialization = deserialization;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonElement serialize(R src, Type typeOfSrc, JsonSerializationContext context) {
        if (serialization == null)
            return STANDARD_SERIALIZATION.apply(src);
        return serialization.apply(src);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public R deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (deserialization == null)
            return standard(json, typeOfT);
        return deserialization.apply(json);
    }

    /**
     * Invokes the standard Gson behavior for serialization
     *
     * @param o Object to serialize
     * @return A {@link JsonElement} that represents this object
     */
    private static JsonElement standard(Object o) {
        return Gsons.DEFAULT.toJsonTree(o);
    }

    /**
     * Invokes the standard Gson behavior for deserialization
     *
     * @param o Element to deserialize
     * @return The deserialized object
     */
    public static <R> R standard(JsonElement o, Type type) {
        return GSON.fromJson(o, type);
    }

}
