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

import io.github.spleefx.util.plugin.Protocol;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class ConfigConverter implements Runnable {

    private static final String ECO = "\n" +
            "# SpleefX economy settings\n" +
            "Economy:\n" +
            "\n" +
            "  # Whether should the plugin get the players' balance from Vault.\n" +
            "  #\n" +
            "  # For example, if the server uses iConomy, the plugin will use iConomy to handle coins.\n" +
            "  # If it uses Essentials Economy, it will use EE to handle coins, and so on.\n" +
            "  #\n" +
            "  # Any economy plugin will work as long as it supports Vault hook.\n" +
            "  #\n" +
            "  # Default value: true\n" +
            "  GetFromVault: true\n" +
            "\n" +
            "  # Whether should SpleefX hook into Vault, as in, SpleefX's economy becomes the one used by Vault.\n" +
            "  #\n" +
            "  # Note that the above setting (\"GetFromVault\") must be set to true for this to work.\n" +
            "  HookIntoVault: false\n";

    private final File config;

    public ConfigConverter(File config) {
        this.config = config;
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
        if (!config.exists()) return;
        try {
            List<String> lines = Files.readAllLines(config.toPath());
            if (Protocol.PROTOCOL == 8) {
                lines.replaceAll(s -> s.replace("BLOCK_LEVER_CLICK", "CLICK"));
            } else {
                lines.replaceAll(s -> s.replace("\"CLICK\"", "\"BLOCK_LEVER_CLICK\""));
            }
            if (lines.stream().noneMatch(s -> s.contains("Economy:"))) {
                lines.addAll(Arrays.asList(ECO.split("\n")));
            }
            Files.write(config.toPath(), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
