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

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * A builder-styled class for {@link PluginCommand}
 */
@SuppressWarnings("unused")
public class PluginCommandBuilder {

    /**
     * The plugin command
     */
    private final PluginCommand command;

    /**
     * Creates a new builder with the specified name
     *
     * @param name Name of the command
     */
    public PluginCommandBuilder(String name, Plugin plugin) {
        command = PluginCommandFactory.create(name, plugin);
    }

    /**
     * Sets the command logic
     *
     * @param logic Command logic to run
     * @return This builder instance
     */
    public PluginCommandBuilder command(CommandExecutor logic) {
        command.setExecutor(logic);
        return this;
    }

    /**
     * Sets the tab logic
     *
     * @param tabCompleter Tab completer to use
     * @return This builder instance
     */
    public PluginCommandBuilder tab(TabCompleter tabCompleter) {
        command.setTabCompleter(tabCompleter);
        return this;
    }

    /**
     * Sets a brief description of this command.
     *
     * @param description new command description
     * @return This builder instance
     */
    public PluginCommandBuilder description(String description) {
        command.setDescription(description);
        return this;
    }

    /**
     * Sets the example usage of this command
     *
     * @param usage new example usage
     * @return This builder instance
     */
    public PluginCommandBuilder usage(String usage) {
        command.setUsage(usage);
        return this;
    }

    /**
     * Sets the list of aliases to request on registration for this command.
     *
     * @param aliases aliases to register to this command
     * @return This builder instance
     */
    public PluginCommandBuilder aliases(List<String> aliases) {
        command.setAliases(aliases);
        return this;
    }

    /**
     * Sets the permission required by users to be able to perform this
     * command
     *
     * @param permission Permission name or null
     * @return This builder instance
     */
    public PluginCommandBuilder permission(String permission) {
        command.setPermission(permission);
        return this;
    }

    /**
     * Sets the message sent when a permission check fails
     *
     * @param permissionMessage new permission message, null to indicate
     *                          default message, or an empty string to indicate no message
     * @return This builder instance
     */
    public PluginCommandBuilder permissionMessage(String permissionMessage) {
        command.setPermissionMessage(permissionMessage);
        return this;
    }

    /**
     * Registers this command
     */
    public void register() {
        PluginCommandFactory.register(command);
    }

    /**
     * Returns the constructed {@link PluginCommand}
     *
     * @return The plugin command
     */
    public PluginCommand build() {
        return command;
    }

}