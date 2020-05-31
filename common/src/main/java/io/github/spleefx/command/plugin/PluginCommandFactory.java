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
package io.github.spleefx.command.plugin;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * A class for handling reflections with {@link PluginCommand} and {@link SimpleCommandMap}
 */
public class PluginCommandFactory {

    /**
     * Constructor of {@link PluginCommand}
     */
    private static Constructor<PluginCommand> constructor;

    /**
     * Field to retrieve a {@link SimpleCommandMap} from the {@link org.bukkit.plugin.PluginManager}
     */
    private static Field commandMapField;

    /**
     * Creates a new instance of a plugin command
     *
     * @param name Name of the command
     * @return The command
     */
    public static PluginCommand create(String name, Plugin plugin) {
        try {
            return constructor.newInstance(name, plugin);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the {@link org.bukkit.command.CommandMap} that is specialized for plugin commands
     *
     * @return The command map
     */
    public static SimpleCommandMap getCommandMap() {
        try {
            return (SimpleCommandMap) commandMapField.get(Bukkit.getPluginManager());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Registers the specified {@link PluginCommand}
     *
     * @param command Command to register
     */
    public static void register(PluginCommand command) {
        getCommandMap().register(command.getPlugin().getDescription().getName(), command);
    }

    static {
        try {
            constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }


}