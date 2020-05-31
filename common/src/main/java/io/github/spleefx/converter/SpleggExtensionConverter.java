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

import com.google.gson.JsonObject;
import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.api.ArenaData;
import org.moltenjson.configuration.direct.DirectConfiguration;
import org.moltenjson.configuration.tree.TreeConfiguration;
import org.moltenjson.json.JsonFile;
import org.moltenjson.utils.JsonUtils;

import java.io.File;
import java.util.StringJoiner;

public class SpleggExtensionConverter implements Runnable {

    //<editor-fold desc="Upgrades JSON" defaultstate="collapsed">
    private static final JsonObject UPGRADES = JsonUtils.getObjectFromString("{\n" +
            "    \"woodenShovel\": {\n" +
            "      \"key\": \"woodenShovel\",\n" +
            "      \"displayName\": \"&eWooden Shovel\",\n" +
            "      \"delay\": 1.0,\n" +
            "      \"default\": true,\n" +
            "      \"price\": 0,\n" +
            "      \"requiredUpgradesBefore\": [],\n" +
            "      \"gameItem\": {\n" +
            "        \"slot\": 0,\n" +
            "        \"type\": \"wooden_shovel\",\n" +
            "        \"count\": 1,\n" +
            "        \"enchantments\": [\n" +
            "          \"infinity:1\"\n" +
            "        ],\n" +
            "        \"displayName\": \"&6Golden Shovel\",\n" +
            "        \"lore\": [],\n" +
            "        \"itemFlags\": [\n" +
            "          \"HIDE_ENCHANTS\"\n" +
            "        ],\n" +
            "        \"unbreakable\": true\n" +
            "      }\n" +
            "    },\n" +
            "    \"stoneShovel\": {\n" +
            "      \"key\": \"stoneShovel\",\n" +
            "      \"displayName\": \"&7Stone Shovel\",\n" +
            "      \"delay\": 0.8,\n" +
            "      \"default\": false,\n" +
            "      \"price\": 1000,\n" +
            "      \"requiredUpgradesBefore\": [\n" +
            "        \"woodenShovel\"\n" +
            "      ],\n" +
            "      \"gameItem\": {\n" +
            "        \"slot\": 0,\n" +
            "        \"type\": \"stone_shovel\",\n" +
            "        \"count\": 1,\n" +
            "        \"enchantments\": [\n" +
            "          \"infinity:1\"\n" +
            "        ],\n" +
            "        \"displayName\": \"&7Stone Shovel\",\n" +
            "        \"lore\": [],\n" +
            "        \"itemFlags\": [\n" +
            "          \"HIDE_ENCHANTS\"\n" +
            "        ],\n" +
            "        \"unbreakable\": true\n" +
            "      }\n" +
            "    },\n" +
            "    \"ironShovel\": {\n" +
            "      \"key\": \"ironShovel\",\n" +
            "      \"displayName\": \"&7Iron Shovel\",\n" +
            "      \"delay\": 0.6,\n" +
            "      \"default\": false,\n" +
            "      \"price\": 2000,\n" +
            "      \"requiredUpgradesBefore\": [\n" +
            "        \"stoneShovel\"\n" +
            "      ],\n" +
            "      \"gameItem\": {\n" +
            "        \"slot\": 0,\n" +
            "        \"type\": \"iron_shovel\",\n" +
            "        \"count\": 1,\n" +
            "        \"enchantments\": [\n" +
            "          \"infinity:1\"\n" +
            "        ],\n" +
            "        \"displayName\": \"&7Iron Shovel\",\n" +
            "        \"lore\": [],\n" +
            "        \"itemFlags\": [\n" +
            "          \"HIDE_ENCHANTS\"\n" +
            "        ],\n" +
            "        \"unbreakable\": true\n" +
            "      }\n" +
            "    },\n" +
            "    \"goldenShovel\": {\n" +
            "      \"key\": \"goldenShovel\",\n" +
            "      \"displayName\": \"&6Golden Shovel\",\n" +
            "      \"delay\": 0.4,\n" +
            "      \"default\": false,\n" +
            "      \"price\": 3000,\n" +
            "      \"requiredUpgradesBefore\": [\n" +
            "        \"ironShovel\"\n" +
            "      ],\n" +
            "      \"gameItem\": {\n" +
            "        \"slot\": 0,\n" +
            "        \"type\": \"golden_shovel\",\n" +
            "        \"count\": 1,\n" +
            "        \"enchantments\": [\n" +
            "          \"infinity:1\"\n" +
            "        ],\n" +
            "        \"displayName\": \"&6Golden Shovel\",\n" +
            "        \"lore\": [],\n" +
            "        \"itemFlags\": [\n" +
            "          \"HIDE_ENCHANTS\"\n" +
            "        ],\n" +
            "        \"unbreakable\": true\n" +
            "      }\n" +
            "    },\n" +
            "    \"diamondShovel\": {\n" +
            "      \"key\": \"diamondShovel\",\n" +
            "      \"displayName\": \"&bDiamond Shovel\",\n" +
            "      \"delay\": 0.2,\n" +
            "      \"default\": false,\n" +
            "      \"price\": 5000,\n" +
            "      \"requiredUpgradesBefore\": [\n" +
            "        \"goldenShovel\"\n" +
            "      ]\n" +
            "    }\n" +
            "  }", ArenaData.GSON);
    private static final JsonObject SPLEGG_SHOP = JsonUtils.getObjectFromString("{\n" +
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
            "          \"\",\n" +
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
            "          \"\",\n" +
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
            "          \"\",\n" +
            "          \"{upgrade_purchased}\"\n" +
            "        ],\n" +
            "        \"itemFlags\": [\n" +
            "          \"HIDE_ENCHANTS\"\n" +
            "        ],\n" +
            "        \"unbreakable\": false\n" +
            "      },\n" +
            "      \"6\": {\n" +
            "        \"purchaseUpgrade\": \"goldenShovel\",\n" +
            "        \"type\": \"golden_shovel\",\n" +
            "        \"count\": 1,\n" +
            "        \"enchantments\": [\n" +
            "          \"infinity:1\"\n" +
            "        ],\n" +
            "        \"displayName\": \"&6Golden Shovel\",\n" +
            "        \"lore\": [\n" +
            "          \"&eDelay: &a{upgrade_delay}\",\n" +
            "          \"&bPrice: &d{upgrade_price}\",\n" +
            "          \"\",\n" +
            "          \"{upgrade_purchased}\"\n" +
            "        ],\n" +
            "        \"itemFlags\": [\n" +
            "          \"HIDE_ENCHANTS\"\n" +
            "        ],\n" +
            "        \"unbreakable\": false\n" +
            "      },\n" +
            "      \"8\": {\n" +
            "        \"purchaseUpgrade\": \"diamondShovel\",\n" +
            "        \"type\": \"diamond_shovel\",\n" +
            "        \"count\": 1,\n" +
            "        \"enchantments\": [\n" +
            "          \"infinity:1\"\n" +
            "        ],\n" +
            "        \"displayName\": \"&bDiamond Shovel\",\n" +
            "        \"lore\": [\n" +
            "          \"&eDelay: &a{upgrade_delay}\",\n" +
            "          \"&bPrice: &d{upgrade_price}\",\n" +
            "          \"\",\n" +
            "          \"{upgrade_purchased}\"\n" +
            "        ],\n" +
            "        \"itemFlags\": [\n" +
            "          \"HIDE_ENCHANTS\"\n" +
            "        ],\n" +
            "        \"unbreakable\": false\n" +
            "      }\n" +
            "    }\n" +
            "  }", ArenaData.GSON);
    //</editor-fold>

    private File extensionsDirectory;

    public SpleggExtensionConverter(File extensionsDirectory) {
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
        convert(new File(extensionsDirectory, "standard" + File.separator + "splegg.json"));
    }

    private void convert(File file) {
        if (file.isFile()) {
            if (!TreeConfiguration.getExtension(file).equals("json"))
                return; // Ignored file
            DirectConfiguration d = DirectConfiguration.of(JsonFile.of(file));
            StringJoiner changed = new StringJoiner(" / ").setEmptyValue("");

            if (d.getFile().getFile().getName().startsWith("splegg")) {
                if (!d.contains("upgradeSystemEnabled")) {
                    d.set("upgradeSystemEnabled", true);
                    changed.add("Add option to toggle the splegg upgrade system");
                }
                if (!d.contains("upgrades")) {
                    d.getContent().add("upgrades", UPGRADES);
                    changed.add("Add splegg upgrade levels");
                }
                if (!d.contains("spleggShop")) {
                    d.getContent().add("spleggShop", SPLEGG_SHOP);
                    changed.add("Add splegg shop section");
                }
            }
            String ch = changed.toString().trim();
            if (!ch.isEmpty()) {
                d.save(Throwable::printStackTrace, ArenaData.GSON);
                SpleefX.logger().info("[SpleggExtensionConverter] Successfully converted old extension " + file.getName() + " to the newer format. (Changes: " + ch.trim() + ")");
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
