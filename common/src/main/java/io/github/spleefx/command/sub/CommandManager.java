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

import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.util.game.Chat;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains all required methods for command management
 */
public class CommandManager {

    // @formatter:off
    public static final CommandManager SPLEEFX = new CommandManager();
    public static final CommandManager SPLEEF = new CommandManager();
    public static final CommandManager BOW_SPLEEF = new CommandManager();
    public static final CommandManager SPLEGG = new CommandManager();
    public static final CommandManager CUSTOM = new CommandManager();
    // @formatter:on

    /**
     * A list of all commands
     */
    private List<PluginSubcommand> commands = new ArrayList<>();

    /**
     * A map which contains all the registered commands. String parameter is the command name,
     * and the {@link PluginSubcommand} parameter is the command instance
     */
    private Map<String, PluginSubcommand> commandsMap = new LinkedHashMap<>();

    /**
     * Registers the given commands
     * <p>
     * Support varargs usage.
     *
     * @param commands Commands to register
     */
    public void registerCommand(PluginSubcommand... commands) {
        for (PluginSubcommand command : commands) {
            this.commands.add(command);
            commandsMap.put(command.getName(), command);
            if (command.getAliases() != null && !command.getAliases().isEmpty())
                command.getAliases().forEach(alias -> commandsMap.put(alias, command));
        }
    }

    /**
     * Returns the command arguments from the message
     *
     * @param args Args to fetch from
     * @return The command arguments
     */
    private String[] getArguments(String[] args) {
        return (String[]) ArrayUtils.subarray(args, 1, args.length);
    }

    public boolean runSub(Command bukkitCommand, CommandSender sender, String[] commandArgs) {
        try {
            String command = commandArgs[0];
            String[] args = getArguments(commandArgs);
            PluginSubcommand sub = commandsMap.get(command);
            GameExtension ex = ExtensionsManager.getFromCommand(bukkitCommand.getName());
            if (sub == null) return false;
            if (!sender.hasPermission(sub.getPermission(bukkitCommand))) {
                Chat.sendUnprefixed(sender, ex.getChatPrefix() + MessageKey.NO_PERMISSION.getText());
                return true;
            }
            if (!sub.handle(bukkitCommand, sender, args))
                sub.getHelpMenu().forEach(e -> Chat.prefix(sender, ex, "&7- " + e));
            return true;
        } catch (StringIndexOutOfBoundsException ignored) {
        } catch (CommandException e) {
            e.send(sender);
            return true; // We don't want to print an invalid command error when there is a higher priority error
        }
        return false;
    }

    /**
     * Returns the map that stores all commands
     *
     * @return The map that stores all the commands
     */
    public Map<String, PluginSubcommand> getCommandsMap() {
        return commandsMap;
    }

    /**
     * Returns a list of all commands
     *
     * @return A List of all commands
     */
    public List<PluginSubcommand> getCommands() {
        return commands;
    }

}