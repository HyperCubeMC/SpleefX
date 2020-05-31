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
import io.github.spleefx.command.sub.CommandException;
import io.github.spleefx.command.sub.PluginSubcommand;
import io.github.spleefx.util.game.Chat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.Arrays;

public class BalanceSubcommand extends PluginSubcommand {

    private static final Permission PERMISSION = new Permission("spleefx.balance", PermissionDefault.TRUE);

    protected static final Permission OTHERS = new Permission("spleefx.balance.others", PermissionDefault.TRUE);

    public BalanceSubcommand() {
        super("bal", (c) -> PERMISSION, "Get your/someone's balance", (c) -> "/spleefx balance [player]");
        aliases = Arrays.asList("balance", "money");
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
        if (!(sender instanceof Player)) throw new CommandException("&cYou must be a player to use this command!");
        switch (args.length) {
            case 0:
                Chat.plugin(sender, "&eYour money: &a$" + SpleefX.getPlugin().getDataProvider().getStatistics(((Player) sender)).getCoinsFormatted(((Player) sender)));
                break;
            case 1:
                if (sender.hasPermission(BalanceSubcommand.OTHERS)) {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
                    Chat.plugin(sender, "&e" + p.getName() + "&a's money: &e$" + SpleefX.getPlugin().getDataProvider().getStatistics(p).getCoinsFormatted(((Player) sender)));
                } else {
                    Chat.plugin(sender, "&eYour money: &a$" + SpleefX.getPlugin().getDataProvider().getStatistics(((Player) sender)).getCoinsFormatted(((Player) sender)));
                }
                break;
        }
        return true;
    }
}
