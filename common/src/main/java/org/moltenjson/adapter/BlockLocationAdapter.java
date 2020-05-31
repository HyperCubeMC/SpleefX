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
import io.github.spleefx.sign.BlockLocation;

import java.lang.reflect.Type;

public class BlockLocationAdapter implements JsonSerializer<BlockLocation>, JsonDeserializer<BlockLocation> {

    public static final BlockLocationAdapter INSTANCE = new BlockLocationAdapter();

    @Override
    public JsonElement serialize(BlockLocation location, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(String.format("%s:%s:%s:%s", location.getWorld(), location.getX(), location.getY(), location.getZ()));
    }

    @Override
    public BlockLocation deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String[] data = jsonElement.getAsString().split(":");
        return new BlockLocation(data[0], Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]));
    }
}
