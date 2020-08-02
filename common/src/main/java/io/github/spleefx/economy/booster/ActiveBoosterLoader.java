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
package io.github.spleefx.economy.booster;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import io.github.spleefx.SpleefX;
import io.github.spleefx.data.PlayerRepository;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class ActiveBoosterLoader {

    public static final Type MAP_TYPE = new TypeToken<Map<UUID, Integer>>() {
    }.getType();

    public static final MapAdapter ADAPTER = new MapAdapter();

    @JsonAdapter(MapAdapter.class)
    @Expose
    private final Map<UUID, Integer> activeBoostersMap = new HashMap<>();

    public Map<OfflinePlayer, BoosterInstance> getActiveBoosters() {
        Map<UUID, Integer> a = activeBoostersMap;
        Map<OfflinePlayer, BoosterInstance> boosters = new HashMap<>();
        for (Entry<UUID, Integer> entry : a.entrySet()) {

            UUID owner = entry.getKey();
            int index = entry.getValue();
            OfflinePlayer p = Bukkit.getOfflinePlayer(owner);
            try {
                boosters.put(p, PlayerRepository.REPOSITORY.lookup(owner).getBoosters().get(index));
            } catch (IndexOutOfBoundsException e) {
                SpleefX.logger().warning("Failed to load an active booster for player " + p.getName() + ".");
            }
        }
        return boosters;
    }

    public Map<UUID, Integer> getActiveBoostersMap() {
        return activeBoostersMap;
    }

    public static class MapAdapter implements JsonSerializer<Map<UUID, Integer>>, JsonDeserializer<Map<UUID, Integer>> {

        @Override
        public Map<UUID, Integer> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            Map<UUID, Integer> map = new HashMap<>();
            JsonArray array = jsonElement.getAsJsonArray();
            for (JsonElement e : array) {
                String[] data = e.getAsString().split(":", 2);
                map.put(UUID.fromString(data[0]), Integer.parseInt(data[1]));
            }
            return map;
        }

        @Override
        public JsonElement serialize(Map<UUID, Integer> uuidIntegerMap, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonArray array = new JsonArray();
            uuidIntegerMap.forEach((uuid, v) -> array.add(new JsonPrimitive(uuid + ":" + v)));
            return array;
        }
    }

}
