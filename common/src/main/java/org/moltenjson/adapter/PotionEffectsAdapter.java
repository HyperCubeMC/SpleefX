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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

public class PotionEffectsAdapter implements JsonSerializer<List<PotionEffect>>, JsonDeserializer<List<PotionEffect>> {

    public static final PotionEffectsAdapter INSTANCE = new PotionEffectsAdapter();

    public static final Type TYPE = new TypeToken<List<PotionEffect>>() {
    }.getType();

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
    public JsonElement serialize(List<PotionEffect> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray array = new JsonArray();
        src.forEach((p) -> array.add(new JsonPrimitive(p.getType().getName() + ":" + p.getDuration() + ":" + p.getAmplifier())));
        return array;
    }

    /**
     * Gson invokes this call-back method during deserialization when it encounters a field of the
     * specified type.
     * <p>In the implementation of this call-back method, you should consider invoking
     * {@link JsonDeserializationContext#deserialize(JsonElement, Type)} method to create objects
     * for any non-trivial field of the returned object. However, you should never invoke it on the
     * the same type passing {@code json} since that will cause an infinite loop (Gson will call your
     * call-back method again).
     *
     * @param json    The Json data being deserialized
     * @param typeOfT The type of the Object to deserialize to
     * @param context
     * @return a deserialized object of the specified type typeOfT which is a subclass of {@code T}
     * @throws JsonParseException if json is not in the expected format of {@code typeofT}
     */
    @Override
    public List<PotionEffect> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        List<PotionEffect> effects = new LinkedList<>();
        for (JsonElement e : array) {
            String[] data = e.getAsString().split(":");
            PotionEffectType type = PotionEffectType.getByName(data[0]);
            if (type == null) {
                SpleefX.logger().warning("Unrecognizable potion effect: " + data[0]);
                continue;
            }
            effects.add(new PotionEffect(type, Integer.parseInt(data[1]), Integer.parseInt(data[2])));
        }
        return effects;
    }
}
