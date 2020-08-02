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
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.data.PlayerProfile;
import io.github.spleefx.data.PlayerRepository;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.util.message.message.Message;
import lombok.Getter;
import lombok.ToString;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a game perk
 */
@ToString
@JsonAdapter(GamePerkAdapter.class)
public class GamePerk {

    public static final File PERKS_FOLDER = new File(SpleefX.getPlugin().getDataFolder(), "perks");

    public static final Map<String, GamePerk> PERKS = new HashMap<>();

    /**
     * The perk key
     */
    @Expose
    @Getter
    private String key;

    @Expose
    @Getter
    private String displayName;

    @Expose
    @JsonAdapter(ListAdapter.class)
    private final List<GameExtension> allowedExtensions = new ArrayList<>();

    @Expose
    @Getter
    public String perkInternalId;

    @Expose
    @Getter
    private PerkPurchaseSettings purchaseSettings;

    public boolean consumeFrom(ArenaPlayer player) {
        if (purchaseSettings.getGamesUsableFor() > 0) return true; // No consuming
        AtomicBoolean b = new AtomicBoolean();
        PlayerRepository.REPOSITORY.apply(player.getPlayer().getUniqueId(), (profile, builder) -> b.set(builder.perks().merge(this, purchaseSettings.getGamesUsableFor() - 1, (i, a) -> i - 1) < 0));
        return b.get();
    }

    @SuppressWarnings("ConstantConditions")
    public boolean canUse(GameExtension extension) {
        return allowedExtensions != null && allowedExtensions.contains(extension);
    }

    public boolean purchase(ArenaPlayer player) {
        int price = getPurchaseSettings().getPrice();
        AtomicBoolean b = new AtomicBoolean(false);
        PlayerProfile stats = player.getStats();
        PlayerRepository.REPOSITORY.apply(player.getPlayer().getUniqueId(), (profile, builder) -> {
            if (stats.getCoins() >= price) {
                if (getPurchaseSettings().getGamesUsableFor() < 0)
                    Message.PERK_ALREADY_PURCHASED.reply(player.getPlayer(), this);
                else {
                    Message.ITEM_PURCHASED.reply(player.getPlayer(), this);
                    builder.perks().merge(this, getPurchaseSettings().getGamesUsableFor(), Integer::sum);
                    builder.subtractCoins(price);
                }
                b.set(true);
            }
        });
        return b.get();
    }

    /**
     * Invoked when the perk is activated
     *
     * @param player The player that activated this perk.
     */
    public void onActivate(ArenaPlayer player) {
    }

    /**
     * Gives this perk to the specified player
     *
     * @param player Player to give
     */
    public void giveToPlayer(ArenaPlayer player) {
    }

    public void load() {
    }

    @SuppressWarnings("unchecked")
    public static <P extends GamePerk> P getPerk(String key) {
        return (P) PERKS.get(key);
    }

    public static class MapAdapter implements JsonSerializer<Map<GamePerk, Integer>>, JsonDeserializer<Map<GamePerk, Integer>> {

        @Override
        public Map<GamePerk, Integer> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            Map<GamePerk, Integer> map = new HashMap<>();
            jsonElement.getAsJsonObject().entrySet().forEach(entry -> map.put(getPerk(entry.getKey()), entry.getValue().getAsInt()));
            return map;
        }

        @Override
        public JsonElement serialize(Map<GamePerk, Integer> gamePerk, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject j = new JsonObject();
            gamePerk.forEach((p, i) -> j.addProperty(p.getKey(), i));
            return j;
        }
    }

    public static class ListAdapter implements JsonSerializer<List<GameExtension>>, JsonDeserializer<List<GameExtension>> {

        @Override
        public JsonElement serialize(List<GameExtension> src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray array = new JsonArray();
            src.forEach(e -> array.add(new JsonPrimitive(e.getKey())));
            return array;
        }

        @Override
        public List<GameExtension> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            List<GameExtension> e = new ArrayList<>();
            json.getAsJsonArray().forEach(k -> e.add(ExtensionsManager.getByKey(k.getAsString())));
            return e;
        }
    }
}