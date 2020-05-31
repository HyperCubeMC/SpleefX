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
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.reflect.Type;

public class LocationAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {

    public static final LocationAdapter INSTANCE = new LocationAdapter();

    @Override
    public JsonElement serialize(Location location, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(String.format("%s:%s:%s:%s:%s:%s", location.getWorld().getName(), location.getBlockX() + 0.5, location.getBlockY(), location.getBlockZ() + 0.5, location.getYaw(), location.getPitch()));
    }

    @Override
    public Location deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String[] data = jsonElement.getAsString().split(":");
        return new Location(Bukkit.getWorld(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2]), Double.parseDouble(data[3]), Float.parseFloat(data[4]), Float.parseFloat(data[5]));
    }
}
