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

import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.api.ArenaData;
import io.github.spleefx.extension.standard.spleef.SpleefExtension.SnowballSettings;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Material;
import org.moltenjson.configuration.direct.DirectConfiguration;
import org.moltenjson.json.JsonFile;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.StringJoiner;

public class SpleefExtensionConverter implements Runnable {

    private File extensionsDirectory;

    public SpleefExtensionConverter(File extensionsDirectory) {
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
        convert(new File(extensionsDirectory, "standard" + File.separator + "spleef.json"));
    }

    private void convert(File file) {
        if (file.isFile()) {
            if (!FilenameUtils.getExtension(file.getName()).equals("json"))
                return; // Ignored file
            DirectConfiguration d = DirectConfiguration.of(JsonFile.of(file));
            StringJoiner changed = new StringJoiner(" / ").setEmptyValue("");

            if (d.getFile().getFile().getName().startsWith("spleef")) {
                if (!d.contains("snowballSettings")) {
                    d.set("snowballSettings", new SnowballSettings());
                    changed.add("Added new snowball settings");
                }
                if (d.contains("snowballSettings")) {
                    Map<String, Object> settings = d.getMap("snowballSettings");
                    if (settings.putIfAbsent("thrownSnowballsRemoveHitBlocks", Collections.singletonList(Material.SNOW_BLOCK)) == null) {
                        d.set("snowballSettings", settings);
                        changed.add("Added thrownSnowballsRemoveHitBlocks");
                    }
                }
            }
            String ch = changed.toString().trim();
            if (!ch.isEmpty()) {
                d.save(Throwable::printStackTrace, ArenaData.GSON);
                SpleefX.logger().info("[SpleefExtensionConverter] Successfully converted old extension " + file.getName() + " to the newer format. (Changes: " + ch.trim() + ")");
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
