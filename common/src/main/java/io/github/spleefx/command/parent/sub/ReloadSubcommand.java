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
package io.github.spleefx.command.parent.sub;

import io.github.spleefx.SpleefX;
import io.github.spleefx.command.sub.PluginSubcommand;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.plugin.PluginSettings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ReloadSubcommand extends PluginSubcommand {

    private static final Permission PERMISSION = new Permission("spleefx.admin.reload");

    private static final List<String> TABS = Arrays.asList("config", "arenas", "statsFile", "joinGuiFile", "messagesFile", "spectator");

    private static final List<String> HELP = Arrays.asList(
            "&ereload &aarenas &7- &dReload the arenas storage &c(Not recommended!)",
            "&ereload &aconfig &7- &dReload the config to update values",
            "&ereload &astatsFile &7- &dReload the statistics GUI file",
            "&ereload &ajoinGuiFile &7- &dReload the join GUI file",
            "&ereload &aspectator &7- &dReload the spectator settings file"
    );

    public ReloadSubcommand() {
        super("reload", (c) -> PERMISSION, "Reloads the specified element", (c) -> "/spleefx reload <arenas | config | statsFile | spectator>");
        super.aliases = Collections.singletonList("rl");
        super.helpMenu = HELP;
    }

    private static final List<String> CONFIRM = Collections.singletonList("confirm");

    /**
     * Handles the command input
     *
     * @param sender Command sender
     * @param args   Extra command arguments
     * @return {@code true} if the command succeed, {@code false} if it is desired to send {@link #getHelpMenu()}.
     */
    @Override
    public boolean handle(Command command, CommandSender sender, String[] args) {
        if (args.length == 0) {
            reloadConfig(sender);
            return true;
        }
        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "config":
                    reloadConfig(sender);
                    return true;
                case "statsfile":
                    reloadStatsFile(sender);
                    return true;
                case "joinguifile":
                    reloadJoinGuiFile(sender);
                    return true;
                case "messagesfile":
                    reloadMessagesFile(sender);
                    return true;
                case "spectator":
                    reloadSpectatorSettings(sender);
                    return true;
                case "arenas":
                    Chat.plugin(sender, "&cAre you sure you want to reload arenas? This is not recommended and may lead to unexpected behavior for running arenas (a restart should fix this).");
                    Chat.plugin(sender, "&cType &e/spleefx reload arenas confirm &cto confirm.");
                    return true;
            }
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("arenas") && args[1].equalsIgnoreCase("confirm")) {
                reloadArenas(sender);
            }
        }
        return true;
    }

    /**
     * Returns a list of tabs for this subcommand.
     *
     * @param args Command arguments. Does <i>NOT</i> channelTo this subcommand.
     * @return A list of all tabs.
     */
    @Override
    public List<String> onTab(CommandSender sender, Command command, String[] args) {
        if (args.length == 0) return TABS;
        if (args.length == 1)
            return TABS.stream().filter(a -> a.startsWith(args[0])).collect(Collectors.toList());
        if (args.length == 2 && args[0].equals("arenas"))
            return CONFIRM;
        return Collections.emptyList();
    }

    private void reloadConfig(CommandSender sender) {
        Chat.plugin(sender, "&eReloading config...");
        SpleefX.getPlugin().reloadConfig();
        Arrays.stream(PluginSettings.values).forEach(PluginSettings::request);
        Chat.plugin(sender, "&aConfig reloaded!");
    }

    private void reloadStatsFile(CommandSender sender) {
        Chat.plugin(sender, "&eReloading statistics-gui.json...");
        SpleefX.getPlugin().getStatsFile().refresh();
        Chat.plugin(sender, "&aFile reloaded!");
    }

    private void reloadSpectatorSettings(CommandSender sender) {
        Chat.plugin(sender, "&eReloading spectator-settings.json...");
        try {
            SpleefX.getPlugin().getConfigurationPack().updateField("spectatorSettings");
        } catch (IOException e) {
            Chat.plugin(sender, "&cCould not reload: &e" + e.getMessage());
            e.printStackTrace();
            return;
        }
        Chat.plugin(sender, "&aFile reloaded!");
    }

    private void reloadJoinGuiFile(CommandSender sender) {
        Chat.plugin(sender, "&eReloading join-gui.json...");
        SpleefX.getPlugin().getJoinGuiFile().refresh();
        Chat.plugin(sender, "&aFile reloaded!");
    }

    private void reloadMessagesFile(CommandSender sender) {
        Chat.plugin(sender, "&eReloading messages.json...");
        MessageKey.load(true);
        Chat.plugin(sender, "&aFile reloaded!");
    }

    private void reloadArenas(CommandSender sender) {
        Chat.plugin(sender, "&eReloading &darenas.json&e...");
        SpleefX.getPlugin().getArenasConfig().refresh();
        Chat.plugin(sender, "&darenas.json &areloaded!");
    }
}