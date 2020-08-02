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

import io.github.spleefx.command.sub.PluginSubcommand;
import io.github.spleefx.data.PlayerRepository;
import io.github.spleefx.economy.booster.BoosterFactory;
import io.github.spleefx.economy.booster.BoosterMenu;
import io.github.spleefx.util.game.Chat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BoosterSubcommand extends PluginSubcommand {

    private static final Permission PERMISSION = new Permission("spleefx.admin.boosters");

    private static final List<String> TABS = Collections.singletonList("add");

    private static final List<String> HELP = Collections.singletonList(
            "&eboosters add <player> <booster type> &7- &dGive the specified booster type to the player"
    );

    public BoosterSubcommand() {
        super("boosters", (c) -> PERMISSION, "Manage boosters", (c) -> "/spleefx boosters [add] <player> <booster type>");
        helpMenu = HELP;
    }

    /**
     * Handles the command logic
     *
     * @param command The bukkit command
     * @param sender  Sender of the command
     * @param args    Command arguments
     * @return {@code false} if it is desired to send the help menu, true if otherwise.
     */
    @Override
    public boolean handle(Command command, CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                Chat.plugin(sender, "&cYou must be a player to use this command!");
                return false;
            }
            Player p = (Player) sender;
            new BoosterMenu(new ArrayList<>(PlayerRepository.REPOSITORY.lookup(p).getBoosters().values())).display(p);
            return true;
        }
        if (args.length < 3) {
            Chat.plugin(sender, "&cInvalid usage. Try &e" + getUsage(command) + "&c.");
            return false;
        }
        if (args[0].equalsIgnoreCase("add")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            BoosterFactory factory = BoosterFactory.get(args[2]);
            if (factory == null) {
                Chat.plugin(sender, "&cInvalid booster type: &e" + args[2] + "&c.");
                return true;
            }
            factory.give(target);
            if (target.getPlayer() != null)
                Chat.plugin(target.getPlayer(), "&aYou have been given a booster of type &e" + factory.getDisplayName() + "&a.");
        }
        return false;
    }

    /**
     * Returns a list of tabs for this subcommand.
     *
     * @param sender
     * @param command The command name.
     * @param args    Command arguments. Does <i>NOT</i> channelTo this subcommand.
     * @return A list of all tabs.
     */
    @Override
    public List<String> onTab(CommandSender sender, Command command, String[] args) {
        switch (args.length) {
            case 1:
                return TABS.stream().filter(c -> c.startsWith(args[0])).collect(Collectors.toList());
            case 2:
                return null;
            case 3:
                return BoosterFactory.BOOSTERS.get().keySet().stream().filter(c -> c.startsWith(args[2])).collect(Collectors.toList());
            default:
                return Collections.emptyList();
        }
    }
}
