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
package io.github.spleefx.command.sub;

import io.github.spleefx.team.TeamColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Represents a sub-command
 */
public abstract class PluginSubcommand {

    /**
     * Represents the command name
     */
    private final String name;

    /**
     * The command permission
     */
    protected Function<Command, Permission> permission;

    /**
     * The command description
     */
    private final String description;

    /**
     * The command aliases
     */
    protected List<String> aliases = Collections.emptyList();

    /**
     * The help menu for the command
     */
    protected List<String> helpMenu = Collections.emptyList();

    /**
     * The command usage
     */
    private final Function<Command, String> usage;

    public PluginSubcommand(String name, Function<Command, Permission> permission, String description, Function<Command, String> usage) {
        this.name = name;
        this.permission = permission;
        this.description = description;
        this.usage = usage;
    }

    /**
     * Returns a list of tabs for this subcommand.
     *
     * @param args    Command arguments. Does <i>NOT</i> channelTo this subcommand.
     * @param command The command name.
     * @return A list of all tabs.
     */
    public List<String> onTab(CommandSender sender, Command command, String[] args) {
        return Collections.emptyList();
    }

    /**
     * Handles the command logic
     *
     * @param command The bukkit command
     * @param sender  Sender of the command
     * @param args    Command arguments
     * @return {@code false} if it is desired to send the help menu, true if otherwise.
     */
    public abstract boolean handle(Command command, CommandSender sender, String[] args);

    /**
     * Checks if the sender is not a player
     *
     * @param sender Sender to check for
     * @return {@code true} if the sender is not a player
     */
    public boolean checkSender(CommandSender sender) {
        return !(sender instanceof Player);
    }

    /**
     * Combines the strings from an array, starting from the specified start point
     *
     * @param strings Strings to combine
     * @param start   Start index
     * @return The combined string
     */
    public String combine(String[] strings, int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < strings.length; i++)
            builder.append(strings[i]).append(" ");
        return builder.toString().trim();
    }

    public String getName() {
        return name;
    }

    public Permission getPermission(Command command) {
        return permission.apply(command);
    }

    public String getDescription() {
        return description;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public List<String> getHelpMenu() {
        return helpMenu;
    }

    public String getUsage(Command command) {
        return usage.apply(command);
    }

    public static String joinNiceString(Object[] elements) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            String s = elements[i] instanceof TeamColor ? ((TeamColor) elements[i]).chat() : elements[i].toString();
            if (i > 0) if (i == elements.length - 1) builder.append(" &7and ");
            else builder.append("&7, ");
            builder.append(s);
        }
        return builder.toString();
    }

}
