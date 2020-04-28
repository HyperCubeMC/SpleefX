/*
 * * Copyright 2019-2020 github.com/ReflxctionDev
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
package io.github.spleefx.command.core;

import io.github.spleefx.command.core.CommandCallback.CommandCallbackException;
import io.github.spleefx.command.core.PluginSubcommand.TabContext;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.util.game.Chat;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The command processor
 */
public class CommandHandler {

    /**
     * A map of all commands
     */
    private Map<String, CommandWrapper> commands = new HashMap<>();

    /**
     * A map of all commands
     */
    private Map<String, CommandWrapper> namesOnly = new HashMap<>();

    /**
     * Registers the specified command
     *
     * @param callback Command to register. Must be annotated with {@link PluginSubcommand}.
     * @return This handler instance to allow chaining
     */
    public CommandHandler register(CommandCallback callback) {
        if (!callback.getClass().isAnnotationPresent(PluginSubcommand.class))
            throw new IllegalArgumentException("Class " + callback.getClass().getName() + " must be annotated with PluginSubcommand!");
        PluginSubcommand p = callback.getClass().getAnnotation(PluginSubcommand.class);
        CommandWrapper wrapper = new CommandWrapper(p.name(), p.description(), p.parameters(), p.aliases(), Arrays.stream(p.helpMenu()).map(Chat::colorize).collect(Collectors.toList()), p.permission(), p.permissionAccess(), p.minimumArguments(), p.requirePlayer(), p.tabCompletions(), callback);
        commands.put(p.name(), wrapper);
        namesOnly.put(p.name(), wrapper);
        for (String alias : p.aliases())
            commands.put(alias, wrapper);
        return this;
    }

    /**
     * Processes the appropriate subcommand
     *
     * @param command The internal Bukkit command
     * @param sender  The command sender
     * @param args    The main command arguments
     */
    public void onCommand(Command command, CommandSender sender, String[] args) {
        CommandContext context = null;
        try {
            CommandWrapper wrapper = commands.get(args[0]);
            if (wrapper == null) {
                MessageKey.UNKNOWN_SUBCOMMAND.send(sender, null, null, null, null,
                        command.getName(), null, -1, ExtensionsManager.getFromCommand(command.getName()));
                throw new CommandCallbackException();
            }
            String[] finalArgs = (String[]) ArrayUtils.subarray(args, 1, args.length);
            context = new CommandContext(sender, finalArgs, ExtensionsManager.getFromCommand(command.getName()), command, wrapper, this);
            if (wrapper.requirePlayer) context.requirePlayer();
            context.requireArgs(wrapper.minimumArgs);
            context.checkPermission(wrapper.generatePermission(context.getExtension()));
            wrapper.callback.onProcess(context);
        } catch (CommandCallbackException e) {
            if (e.getMessage().isEmpty()) return;
            context.reply(e.getMessage());
        }
    }

    public Map<String, CommandWrapper> getCommands() {
        return commands;
    }

    public Map<String, CommandWrapper> getNamesOnly() {
        return namesOnly;
    }

    /**
     * Represents a parent command
     */
    public static class ParentCommand implements TabExecutor {

        /**
         * A default fallback when no arguments are specified
         */
        private static final String[] HELP = new String[]{"help"};

        /**
         * The command handler
         */
        private CommandHandler commandHandler = new CommandHandler();

        /**
         * Executes the given command, returning its success.
         * <br>
         * If false is returned, then the "usage" plugin.yml entry for this command
         * (if defined) will be sent to the player.
         *
         * @param sender  Source of the command
         * @param command Command which was executed
         * @param label   Alias of the command which was used
         * @param args    Passed command arguments
         * @return true if a valid command, otherwise false
         */
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length == 0) args = HELP;
            commandHandler.onCommand(command, sender, args);
            return true;
        }

        /**
         * Requests a list of possible completions for a command argument.
         *
         * @param sender  Source of the command.  For players tab-completing a
         *                command inside of a command block, this will be the player, not
         *                the command block.
         * @param command Command which was executed
         * @param alias   The alias used
         * @param args    The arguments passed to the command, including final
         *                partial argument to be completed and command label
         * @return A List of possible completions for the final argument, or null
         * to default to the command executor
         */
        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            if (args.length == 1)
                return commandHandler.commands.values().stream().filter(a -> a.name.startsWith(args[0]))
                        .filter(s -> sender.hasPermission(s.generatePermission(ExtensionsManager.getFromCommand(command.getName()))))
                        .map(c -> c.name)
                        .distinct()
                        .collect(Collectors.toList());
            if (args.length > 1) {
                String[] finalArgs = (String[]) ArrayUtils.subarray(args, 1, args.length);
                if (finalArgs.length == 0) return Collections.emptyList();
                CommandWrapper wrapper = commandHandler.commands.get(args[0]);
                String tab = wrapper.tab;
                if (wrapper.tab.equals(PluginSubcommand.DEFAULT_COMPLETION)) return Collections.emptyList();
                TabContext context = new TabContext(finalArgs, sender, wrapper, command, commandHandler);
                String[] tabs = tab.split(" ");

                String thisTab;
                try {
                    thisTab = tabs[finalArgs.length - 1];
                } catch (ArrayIndexOutOfBoundsException e) {
                    return Collections.emptyList();
                }
                if (thisTab.startsWith("@")) {
                    List<String> text = CommandResolvers.RESOLVERS
                            .getTab(thisTab.substring(1), context);
                    return text == null ? null : text
                            .stream()
                            .distinct()
                            .filter(c -> c.startsWith(finalArgs[finalArgs.length - 1]))
                            .collect(Collectors.toList());
                }
                return Arrays
                        .stream(StringUtils.split(thisTab, "|"))
                        .map(a -> a.replace("~~", " "))
                        .distinct()
                        .filter(a -> a.startsWith(finalArgs[finalArgs.length - 1]))
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        }

        /**
         * Registers/Contains the specified command in the processing
         *
         * @param callback Command callback to register
         * @return This command
         */
        public ParentCommand contain(CommandCallback callback) {
            commandHandler.register(callback);
            return this;
        }

        /**
         * Registers/Contains the specified command in the processing, and allows the registry to
         * use the command handler instance
         *
         * @param withHandler Function to register
         * @return This command
         */
        public ParentCommand contain(Function<CommandHandler, CommandCallback> withHandler) {
            commandHandler.register(withHandler.apply(commandHandler));
            return this;
        }

        /**
         * Creates a new command
         */
        public static ParentCommand create() {
            return new ParentCommand();
        }

    }

}