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
package io.github.spleefx.converter;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.ArenaStage;
import io.github.spleefx.arena.api.ArenaData;
import io.github.spleefx.extension.GameExtension.ArmorSlots;
import io.github.spleefx.extension.GameExtension.QuitItem;
import io.github.spleefx.extension.GameExtension.ScoreboardEvent;
import io.github.spleefx.extension.GameExtension.SenderType;
import io.github.spleefx.extension.ItemHolder;
import io.github.spleefx.extension.ability.DoubleJumpHandler.DataHolder;
import io.github.spleefx.extension.ability.TripleArrowsAbility.Settings;
import io.github.spleefx.scoreboard.ScoreboardHolder;
import io.github.spleefx.util.code.MapBuilder;
import org.moltenjson.configuration.direct.DirectConfiguration;
import org.moltenjson.configuration.tree.TreeConfiguration;
import org.moltenjson.json.JsonFile;
import org.moltenjson.utils.JsonUtils;

import java.io.File;
import java.util.*;

/**
 * A task that converts files from the old extensions format to the newer one. Useful when updating to avoid breaking
 * backwards compatibility
 */
public class LegacyExtensionConverter implements Runnable {

    private File extensionsDirectory;

    //<editor-fold desc="Upgrades JSON" defaultstate="collapsed">
    private static final JsonObject UPGRADES = JsonUtils.getObjectFromString("{\"woodenShovel\": {\n" +
            "      \"key\": \"woodenShovel\",\n" +
            "      \"delay\": 1.0,\n" +
            "      \"default\": true,\n" +
            "      \"price\": 0,\n" +
            "      \"requiredUpgradesBefore\": []\n" +
            "    },\n" +
            "    \"stoneShovel\": {\n" +
            "      \"key\": \"stoneShovel\",\n" +
            "      \"delay\": 0.8,\n" +
            "      \"default\": false,\n" +
            "      \"price\": 1000,\n" +
            "      \"requiredUpgradesBefore\": [\n" +
            "        \"woodenShovel\"\n" +
            "      ]\n" +
            "    },\n" +
            "    \"ironShovel\": {\n" +
            "      \"key\": \"ironShovel\",\n" +
            "      \"delay\": 0.6,\n" +
            "      \"default\": false,\n" +
            "      \"price\": 2000,\n" +
            "      \"requiredUpgradesBefore\": [\n" +
            "        \"stoneShovel\"\n" +
            "      ]\n" +
            "    },\n" +
            "    \"goldenShovel\": {\n" +
            "      \"key\": \"goldenShovel\",\n" +
            "      \"delay\": 0.4,\n" +
            "      \"default\": false,\n" +
            "      \"price\": 3000,\n" +
            "      \"requiredUpgradesBefore\": [\n" +
            "        \"ironShovel\"\n" +
            "      ]\n" +
            "    },\n" +
            "    \"diamondShovel\": {\n" +
            "      \"key\": \"diamondShovel\",\n" +
            "      \"delay\": 0.2,\n" +
            "      \"default\": false,\n" +
            "      \"price\": 5000,\n" +
            "      \"requiredUpgradesBefore\": [\n" +
            "        \"stoneShovel\"\n" +
            "      ]\n" +
            "    }}", ArenaData.GSON);
    private static final JsonObject SPLEGG_SHOP = JsonUtils.getObjectFromString(" {\n" +
            "    \"title\": \"&2Splegg Upgrades\",\n" +
            "    \"rows\": 1,\n" +
            "    \"items\": {\n" +
            "      \"0\": {\n" +
            "        \"purchaseUpgrade\": \"woodenShovel\",\n" +
            "        \"type\": \"wooden_shovel\",\n" +
            "        \"count\": 1,\n" +
            "        \"enchantments\": [\n" +
            "          \"infinity:1\"\n" +
            "        ],\n" +
            "        \"displayName\": \"&eWooden Shovel\",\n" +
            "        \"lore\": [\n" +
            "          \"&eDelay: &a{upgrade_delay}\",\n" +
            "          \"&bPrice: &d{upgrade_price}\",\n" +
            "          \"{upgrade_purchased}\"\n" +
            "        ],\n" +
            "        \"itemFlags\": [\n" +
            "          \"HIDE_ENCHANTS\"\n" +
            "        ],\n" +
            "        \"unbreakable\": false\n" +
            "      },\n" +
            "      \"2\": {\n" +
            "        \"purchaseUpgrade\": \"stoneShovel\",\n" +
            "        \"type\": \"stone_shovel\",\n" +
            "        \"count\": 1,\n" +
            "        \"enchantments\": [\n" +
            "          \"infinity:1\"\n" +
            "        ],\n" +
            "        \"displayName\": \"&7Stone Shovel\",\n" +
            "        \"lore\": [\n" +
            "          \"&eDelay: &a{upgrade_delay}\",\n" +
            "          \"&bPrice: &d{upgrade_price}\",\n" +
            "          \"{upgrade_purchased}\"\n" +
            "        ],\n" +
            "        \"itemFlags\": [\n" +
            "          \"HIDE_ENCHANTS\"\n" +
            "        ],\n" +
            "        \"unbreakable\": false\n" +
            "      },\n" +
            "      \"4\": {\n" +
            "        \"purchaseUpgrade\": \"ironShovel\",\n" +
            "        \"type\": \"iron_shovel\",\n" +
            "        \"count\": 1,\n" +
            "        \"enchantments\": [\n" +
            "          \"infinity:1\"\n" +
            "        ],\n" +
            "        \"displayName\": \"&7Iron Shovel\",\n" +
            "        \"lore\": [\n" +
            "          \"&eDelay: &a{upgrade_delay}\",\n" +
            "          \"&bPrice: &d{upgrade_price}\",\n" +
            "          \"{upgrade_purchased}\"\n" +
            "        ],\n" +
            "        \"itemFlags\": [\n" +
            "          \"HIDE_ENCHANTS\"\n" +
            "        ],\n" +
            "        \"unbreakable\": false\n" +
            "      }\n" +
            "    },\n" +
            "    \"4\": {\n" +
            "      \"purchaseUpgrade\": \"ironShovel\",\n" +
            "      \"type\": \"iron_shovel\",\n" +
            "      \"count\": 1,\n" +
            "      \"enchantments\": [\n" +
            "        \"infinity:1\"\n" +
            "      ],\n" +
            "      \"displayName\": \"&7Iron Shovel\",\n" +
            "      \"lore\": [\n" +
            "        \"&eDelay: &a{upgrade_delay}\",\n" +
            "        \"&bPrice: &d{upgrade_price}\",\n" +
            "        \"{upgrade_purchased}\"\n" +
            "      ],\n" +
            "      \"itemFlags\": [\n" +
            "        \"HIDE_ENCHANTS\"\n" +
            "      ],\n" +
            "      \"unbreakable\": false\n" +
            "    },\n" +
            "    \"6\": {\n" +
            "      \"purchaseUpgrade\": \"goldenShovel\",\n" +
            "      \"type\": \"golden_shovel\",\n" +
            "      \"count\": 1,\n" +
            "      \"enchantments\": [\n" +
            "        \"infinity:1\"\n" +
            "      ],\n" +
            "      \"displayName\": \"&6Golden Shovel\",\n" +
            "      \"lore\": [\n" +
            "        \"&eDelay: &a{upgrade_delay}\",\n" +
            "        \"&bPrice: &d{upgrade_price}\",\n" +
            "        \"{upgrade_purchased}\"\n" +
            "      ],\n" +
            "      \"itemFlags\": [\n" +
            "        \"HIDE_ENCHANTS\"\n" +
            "      ],\n" +
            "      \"unbreakable\": false\n" +
            "    },\n" +
            "    \"8\": {\n" +
            "      \"purchaseUpgrade\": \"diamondShovel\",\n" +
            "      \"type\": \"diamond_shovel\",\n" +
            "      \"count\": 1,\n" +
            "      \"enchantments\": [\n" +
            "        \"infinity:1\"\n" +
            "      ],\n" +
            "      \"displayName\": \"&bDiamond Shovel\",\n" +
            "      \"lore\": [\n" +
            "        \"&eDelay: &a{upgrade_delay}\",\n" +
            "        \"&bPrice: &d{upgrade_price}\",\n" +
            "        \"{upgrade_purchased}\"\n" +
            "      ],\n" +
            "      \"itemFlags\": [\n" +
            "        \"HIDE_ENCHANTS\"\n" +
            "      ],\n" +
            "      \"unbreakable\": false\n" +
            "    }\n" +
            "  }", ArenaData.GSON);
    //</editor-fold>

    private static final JsonArray RUN_COMMANDS = new JsonArray();

    public LegacyExtensionConverter(File extensionsDirectory) {
        this.extensionsDirectory = extensionsDirectory;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        if (!extensionsDirectory.exists()) return; // There are no files to convert
        Arrays.stream(Objects.requireNonNull(extensionsDirectory.listFiles())).forEach(this::convert);
    }

    private void convert(File file) {
        if (file.isFile()) {
            if (!TreeConfiguration.getExtension(file).equals("json"))
                return; // Ignored file
            DirectConfiguration d = DirectConfiguration.of(JsonFile.of(file));
            StringJoiner changed = new StringJoiner(" / ").setEmptyValue("");
            if (!d.contains("runCommandsForFFAWinners") && d.contains("runCommandsForWinners")) { // File is already in the new form
                Map<Integer, Map<SenderType, List<String>>> commands = d.get("runCommandsForWinners", new TypeToken<Map<Integer, Map<SenderType, List<String>>>>() {
                }.getType());
                d.remove("runCommandsByWinner");
                d.remove("runCommandsByConsoleForWinner");
                d.remove("runCommandsForWinners");
                d.set("runCommandsForFFAWinners", commands);
                d.set("runCommandsForTeamWinners", commands);
                changed.add("Split rewards to 2 sections: FFA winners and teams winners");
            }
            if (!d.contains("scoreboard")) {
                ScoreboardHolder sb = new ScoreboardHolder();
                d.set("scoreboard", MapBuilder.of(new LinkedHashMap<>())
                        .put(ScoreboardEvent.WAITING_IN_LOBBY, sb)
                        .put(ScoreboardEvent.COUNTDOWN_AND_WAITING, sb)
                        .put(ScoreboardEvent.COUNTDOWN_ARENA_FULL, sb)
                        .put(ScoreboardEvent.GAME_ACTIVE, sb)
                        .build());
                changed.add("Added scoreboards");
            } else {
                Map<String, ScoreboardHolder> oldScoreboards = d.get("scoreboard", new TypeToken<Map<String, ScoreboardHolder>>() {
                }.getType());
                if (oldScoreboards.containsKey("ACTIVE")) { // old scoreboards.
                    ScoreboardHolder waiting = oldScoreboards.remove(ArenaStage.WAITING.name());
                    ScoreboardHolder countdown = oldScoreboards.remove(ArenaStage.COUNTDOWN.name());
                    ScoreboardHolder active = oldScoreboards.remove(ArenaStage.ACTIVE.name());
                    if (waiting != null)
                        oldScoreboards.put(ScoreboardEvent.WAITING_IN_LOBBY.name(), waiting);
                    if (countdown != null) {
                        oldScoreboards.put(ScoreboardEvent.COUNTDOWN_AND_WAITING.name(), countdown);
                        oldScoreboards.put(ScoreboardEvent.COUNTDOWN_ARENA_FULL.name(), countdown);
                    }
                    if (active != null)
                        oldScoreboards.put(ScoreboardEvent.GAME_ACTIVE.name(), active);
                    d.set("scoreboard", oldScoreboards);
                    changed.add("Change scoreboards into new format");
                }
            }
            if (!d.contains("doubleJump")) {
                d.set("doubleJump", new DataHolder());
                changed.add("Added double jumps");
            }
            if (!d.contains("giveDroppedItems")) {
                d.set("giveDroppedItems", true);
                changed.add("Added giveDroppedItems option");
            }
            if (!d.contains("runCommandsWhenGameFills")) {
                d.set("runCommandsWhenGameFills", RUN_COMMANDS);
                changed.add("Added runCommandsWhenGameFills");
            }
            if (!d.contains("runCommandsWhenGameStarts")) {
                d.set("runCommandsWhenGameStarts", RUN_COMMANDS);
                changed.add("Added runCommandsWhenGameStarts");
            }
            if (!d.contains("armorToAdd")) {
                d.set("armorToAdd", ImmutableMap.of(ArmorSlots.helmet, new ItemHolder().setType("diamond_helmet").setUnbreakable(true)));
                changed.add("Added armorToAdd option");
            }
            if (d.contains("scoreboard") && d.getMap("scoreboard").containsKey("enabled")) { // old scoreboard format
                ScoreboardHolder holder = d.get("scoreboard", ScoreboardHolder.class, ArenaData.GSON);
                ScoreboardHolder sb = new ScoreboardHolder();
                d.set("scoreboard", MapBuilder.of(new LinkedHashMap<>()).putIfAbsent(ArenaStage.ACTIVE, holder)
                        .putIfAbsent(ArenaStage.WAITING, sb).putIfAbsent(ArenaStage.COUNTDOWN, sb).build());
                changed.add("Add a new scoreboard for each arena stage");
            }
            if (d.contains("quitItemSlot")) {
                QuitItem quitItem = new QuitItem(d.get("quitItem", ItemHolder.class, ArenaData.GSON));
                quitItem.slot = d.getInt("quitItemSlot");
                d.remove("quitItemSlot");
                d.set("quitItem", quitItem);
                changed.add("Added new Quit Item settings");
            }
            if (d.getFile().getFile().getName().startsWith("bow_spleef") && !d.contains("tripleArrows")) {
                d.set("tripleArrows", new Settings());
                changed.add("Add triple arrows");
            }
            String ch = changed.toString().trim();
            if (!ch.isEmpty()) {
                d.save(Throwable::printStackTrace, ArenaData.GSON);
                SpleefX.logger().info("[LegacyExtensionConverter] Successfully converted old extension " + file.getName() + " to the newer format. (Changes: " + ch.trim() + ")");
            }
        } else { // it is a directory not a file, so convert recursively
            File[] files = file.listFiles();
            if (files == null) return;
            for (File old : files) {
                convert(old);
            }
        }
    }
}