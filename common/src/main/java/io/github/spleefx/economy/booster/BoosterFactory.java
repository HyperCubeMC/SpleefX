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
import io.github.spleefx.SpleefX;
import io.github.spleefx.extension.ItemHolder;
import io.github.spleefx.util.plugin.Duration;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.moltenjson.configuration.select.SelectKey;
import org.moltenjson.configuration.select.SelectionHolder;

import java.lang.reflect.Type;
import java.util.*;

public class BoosterFactory {

    @SelectKey("consumeActiveBoostersWhenOffline")
    public static final SelectionHolder<Boolean> CONSUME_WHILE_OFFLINE = new SelectionHolder<>(false);

    @SelectKey("maximumAmountOfActiveBoostersAllowed")
    public static final SelectionHolder<Integer> ALLOW_MULTIPLE = new SelectionHolder<>(1);

    @SelectKey("canBoostersBePaused")
    public static final SelectionHolder<Boolean> CAN_BE_PAUSED = new SelectionHolder<>(true);

    @SelectKey("boostingMethod")
    public static final SelectionHolder<BoostingMethod> BOOSTING_METHOD = new SelectionHolder<>(BoostingMethod.SUM_MULTIPLIERS);

    @SelectKey("boosterMenuTitle")
    public static final SelectionHolder<String> BOOSTER_MENU_TITLE = new SelectionHolder<>("&5Your boosters");

    @SelectKey("boosters")
    public static final SelectionHolder<Map<String, BoosterFactory>> BOOSTERS = new SelectionHolder<>(new LinkedHashMap<>());

    /**
     * The booster key
     */
    @Expose
    private String key;

    /**
     * The booster's display name
     */
    @Expose
    private String displayName;

    /**
     * Whether is the booster enabled or not
     */
    @Expose
    private boolean enabled;

    /**
     * The booster multiplier
     */
    @Expose
    private double multiplier;

    /**
     * The booster's duration
     */
    @Expose
    private Duration duration;

    @Expose
    private Map<BoosterState, ItemHolder> items;

    public BoosterFactory(String key, String displayName, double multiplier, Duration duration, Map<BoosterState, ItemHolder> items) {
        this.key = key;
        this.displayName = displayName;
        this.enabled = true;
        this.multiplier = multiplier;
        this.duration = duration;
        this.items = items;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public Duration getDuration() {
        return duration;
    }

    /**
     * Returns the new sum
     *
     * @param player
     * @param original
     * @return
     */
    public static int boost(OfflinePlayer player, int original) {
        List<BoosterInstance> boosters = new ArrayList<>(SpleefX.getPlugin().getDataProvider().getStatistics(player).getActiveBoosters());
        if (boosters.size() == 0) return original;
        if (boosters.size() == 1) return original + boosters.get(0).applyMultiplier(original);
        boosters.sort(Comparator.comparingDouble(BoosterInstance::getMultiplier));
        switch (BOOSTING_METHOD.get()) {
            case USE_HIGHEST:
                return original + boosters.get(0).applyMultiplier(original);
            case MULTIPLICATIVE: {
                double result = boosters.stream().mapToDouble(BoosterInstance::getMultiplier).reduce(1, (a, b1) -> a * b1);
                return (int) (original + (original * result));
            }
            case SUM_MULTIPLIERS:
                double result = boosters.stream().mapToDouble(BoosterInstance::getMultiplier).sum();
                return (int) (original + (original * result));
        }
        return original;
    }

    public Map<BoosterState, ItemHolder> getItems() {
        return items;
    }

    public BoosterInstance give(OfflinePlayer player) {
        BoosterInstance booster = new BoosterInstance(player.getUniqueId(), this, getMultiplier(), getDuration());
        SpleefX.getPlugin().getDataProvider().addBooster(player, booster);
        return booster;
    }

    public static BoosterFactory get(String key) {
        return BOOSTERS.get().get(key);
    }

    public static class FactoryStringAdapter implements JsonSerializer<BoosterFactory>, JsonDeserializer<BoosterFactory> {

        @Override
        public BoosterFactory deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return BOOSTERS.get().get(jsonElement.getAsString());
        }

        @Override
        public JsonElement serialize(BoosterFactory boosterFactory, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(boosterFactory.key);
        }
    }

    public static class BoosterListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void onPlayerQuit(PlayerQuitEvent event) {
            if (!CONSUME_WHILE_OFFLINE.get())
                SpleefX.getPlugin().getDataProvider().getStatistics(event.getPlayer()).getActiveBoosters().forEach(BoosterInstance::pause);
        }
    }

}
