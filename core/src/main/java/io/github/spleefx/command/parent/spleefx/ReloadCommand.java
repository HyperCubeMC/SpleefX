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
package io.github.spleefx.command.parent.spleefx;

import io.github.spleefx.SpleefX;
import io.github.spleefx.command.core.CommandCallback;
import io.github.spleefx.command.core.CommandContext;
import io.github.spleefx.command.core.PluginSubcommand;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.plugin.PluginSettings;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

@PluginSubcommand(
        name = "reload",
        permission = "spleefx.admin.reload",
        tabCompletions = "config|arenas|statsFile|joinGuiFile|messagesFile @rlConfirm",
        description = "Reloads the specified element",
        helpMenu = {
                "&ereload &aarenas &7- &dReload the arenas storage &c(Not recommended!)",
                "&ereload &aconfig &7- &dReload the config to update values",
                "&ereload &astatsFile &7- &dReload the statistics GUI file",
                "&ereload &ajoinGuiFile &7- &dReload the join GUI file"
        },
        parameters = "(config / arenas / statsFile / joinGuiFile / messagesFile)",
        aliases = {"rl"}
)
public class ReloadCommand implements CommandCallback {

    /**
     * Invoked when the command is processed
     *
     * @param context Context of the command (data)
     */
    @Override public void onProcess(CommandContext context) {
        CommandSender sender = context.getSender();
        String[] args = context.getArgs();
        if (args.length == 0) {
            reloadConfig(sender);
            return;
        }
        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "config":
                    reloadConfig(sender);
                    break;
                case "statsfile":
                    reloadStatsFile(sender);
                    break;
                case "joinguifile":
                    reloadJoinGuiFile(sender);
                    break;
                case "messagesfile":
                    reloadMessagesFile(sender);
                    break;
                case "arenas":
                    Chat.plugin(sender, "&cAre you sure you want to reload arenas? This is not recommended and may lead to unexpected behavior for running arenas (a restart should fix this).");
                    Chat.plugin(sender, "&cType &e/spleefx reload arenas confirm &cto confirm.");
                    break;
            }
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("arenas") && args[1].equalsIgnoreCase("confirm")) {
                reloadArenas(sender);
            }
        }
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
