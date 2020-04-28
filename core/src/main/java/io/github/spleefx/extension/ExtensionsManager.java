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
package io.github.spleefx.extension;

import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.api.ArenaData;
import io.github.spleefx.arena.spleef.SpleefArena;
import io.github.spleefx.command.parent.CommandsContainer;
import io.github.spleefx.command.plugin.PluginCommandBuilder;
import io.github.spleefx.extension.GameExtension.ExtensionType;
import io.github.spleefx.extension.standard.bowspleef.BowSpleefExtension;
import io.github.spleefx.extension.standard.splegg.SpleggExtension;
import org.moltenjson.json.JsonFile;
import org.moltenjson.json.JsonReader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExtensionsManager {

    /**
     * The parent extensions folder
     */
    public static final File EXTENSIONS_FOLDER = new File(SpleefX.getPlugin().getDataFolder(), "extensions");

    /**
     * The custom extensions folder
     */
    public static final File CUSTOM_FOLDER = new File(EXTENSIONS_FOLDER, "custom");

    /**
     * The standard extensions folder
     */
    public static final File STANDARD_FOLDER = new File(EXTENSIONS_FOLDER, "standard");

    /**
     * Maps extensions by keys
     */
    public static final Map<String, GameExtension> EXTENSIONS = new HashMap<>();

    /**
     * Extensions commands
     */
    private static final Map<String, GameExtension> COMMANDS = new HashMap<>();

    /**
     * Returns an extension from the specified key
     *
     * @param key Key to retrieve from
     * @return The extension
     */
    public static GameExtension getByKey(String key) {
        return EXTENSIONS.get(key);
    }

    /**
     * Returns an extension from the specified command name
     *
     * @param commandName Command name to retrieve from
     * @return The extension
     */
    public static GameExtension getFromCommand(String commandName) {
        return COMMANDS.get(commandName);
    }

    private SpleefX plugin;

    public ExtensionsManager(SpleefX plugin) {
        this.plugin = plugin;
    }

    public GameExtension load(String key) {
        GameExtension e = plugin.getExtensions().lazyLoad(key, GameExtension.class, ExtensionType.CUSTOM);
        if (e == null) return null;
        mapExtension(key, e);
        if (
                !e.getKey().equals(SpleefArena.EXTENSION.getKey()) &&
                        !e.getKey().equals(BowSpleefExtension.EXTENSION.getKey()) &&
                        !e.getKey().equals(SpleggExtension.EXTENSION.getKey()))
            e.getExtensionCommands().forEach(command -> new PluginCommandBuilder(command, plugin)
                    .command(CommandsContainer.CUSTOM)
                    .register());
        return e;
    }

    /**
     * Returns the specified extension from the name and type, and deserializes it according to the instance.
     *
     * @param name           Name of the extension
     * @param type           Type of the extension
     * @param extensionClass Class of the extension
     * @param <R>            Extension reference.
     * @return The specified extension loaded from the file
     */
    public static <R extends GameExtension> R getExtension(String name, ExtensionType type, Class<R> extensionClass) {
        File directory = type == ExtensionType.CUSTOM ? CUSTOM_FOLDER : STANDARD_FOLDER;
        try (JsonReader reader = JsonReader.of(JsonFile.of(directory, name + ".json"))) {
            R extension = reader.deserializeAs(extensionClass, ArenaData.GSON);
            mapExtension(name, extension);
            return extension;
        } catch (IOException e) {
            SpleefX.logger().severe("Failed to load extension\"" + name + "\".");
            throw new RuntimeException(e);
        }
    }

    public static boolean mapExtension(String key, GameExtension extension) {
        extension.getExtensionCommands().forEach(c -> COMMANDS.put(c, extension));
        return EXTENSIONS.put(key, extension) == null;
    }

}
