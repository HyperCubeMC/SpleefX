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
package io.github.spleefx.command.sub.base;

import io.github.spleefx.command.sub.CommandManager;
import io.github.spleefx.command.sub.PluginSubcommand;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.util.game.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HelpSubcommand extends PluginSubcommand {

    private static final List<String> HELP = Collections.singletonList(
            "&ehelp &a[subcommand] &7- &dGet help for a specific sub-command"
    );

    private static final List<String> ALIASES = Collections.singletonList(
            "?"
    );

    private CommandManager commandManager;

    public HelpSubcommand(CommandManager commandManager) {
        super("help",
                (c) -> new Permission("spleefx." + c.getName() + ".help", PermissionDefault.TRUE),
                "Display the help menu",
                (c) -> "/" + c.getName() + " help [subcommand]");
        this.commandManager = commandManager;
        super.helpMenu = HELP;
        super.aliases = ALIASES;
    }

    /**
     * Handles the command input
     *
     * @param sender Command sender
     * @param args   Extra command arguments
     * @return {@code true} if the command succeed, {@code false} if it is desired to send {@link #getHelpMenu()}.
     */
    @Override
    public boolean handle(Command command, CommandSender sender, String[] args) {
        String prefix = command.getName().equals("spleefx") ? MessageKey.prefix() : ExtensionsManager.getFromCommand(command.getName()).getChatPrefix();
        if (args.length == 0) {
            commandManager.getCommands().stream().filter(c -> sender.hasPermission(c.getPermission(command))).forEachOrdered(c -> Chat.sendUnprefixed(sender, prefix + "&e" + c.getUsage(command) + " &7- &d" + c.getDescription()));
            Chat.sendUnprefixed(sender, prefix + "&dRun &e/" + command.getName() + " help <subcommand> &dfor more details.");
            return true;
        }
        PluginSubcommand subcommand = commandManager.getCommandsMap().get(args[0]);
        if (subcommand == null) {
            Chat.sendUnprefixed(sender, prefix + "&cInvalid subcommand: &e" + args[0]);
            return true;
        }
        if (!sender.hasPermission(subcommand.getPermission(command))) {
            Chat.sendUnprefixed(sender, prefix + "&cYou do not have access to this command");
            return true;
        }
        if (subcommand.getHelpMenu() == null || subcommand.getHelpMenu().isEmpty())
            Chat.sendUnprefixed(sender, prefix + "&cNo help menu for this subcommand.");
        else
            subcommand.getHelpMenu().forEach(s -> Chat.sendUnprefixed(sender, prefix + "&e" + s));
        return true;
    }

    @Override
    public List<String> onTab(CommandSender sender, Command command, String[] args) {
        if (args.length == 1)
            return commandManager.getCommands().stream().filter(c -> sender.hasPermission(c.getPermission(command)) && c.getName().startsWith(args[0])).map(PluginSubcommand::getName).collect(Collectors.toList());
        return Collections.emptyList();
    }
}