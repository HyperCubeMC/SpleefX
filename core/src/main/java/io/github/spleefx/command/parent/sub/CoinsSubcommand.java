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
import io.github.spleefx.data.GameStats;
import io.github.spleefx.economy.booster.BoosterFactory;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.plugin.PluginSettings;
import net.milkbowl.vault.economy.plugins.Economy_SpleefX;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CoinsSubcommand extends PluginSubcommand {

    private static final Permission PERMISSION = new Permission("spleefx.admin.coins");

    private static final List<String> TABS = Arrays.asList("add", "addboosted", "take", "set", "reset");

    private static final List<String> NUMBERS = IntStream.range(0, 1001).filter(i -> i % 100 == 0).mapToObj(Integer::toString).collect(Collectors.toList());

    private static final List<String> HELP = Arrays.asList(
            "&ecoins add <player> <amount> &7- &dGive the specified amount of coins to the player",
            "&ecoins addboosted <player> <amount> &7- &dGive the specified amount of coins to the player, while taking all the active player boosters into consideration",
            "&ecoins take <player> <amount> &7- &dTake the specified amount of coins from the player",
            "&ecoins set <player> <amount> &7- &dSet the amount of coins of the player",
            "&ecoins reset <player> &7- &dReset the player's balance"
    );

    public CoinsSubcommand() {
        super("coins", (c) -> PERMISSION, "Manage a user's coins", (c) -> "/spleefx coins <add|addboosted|take|set|reset> <player> <[new value]>");
        helpMenu = HELP;
    }

    /**
     * Handles the command logic
     *
     * @param command The bukkit command
     * @param sender  Sender of the command
     * @param args    Command arguments
     * @return {@code true} if it is desired to send the help menu, false if otherwise.
     */
    @Override
    public boolean handle(Command command, CommandSender sender, String[] args) {
        if (GameStats.VAULT_EXISTS.get() && ((boolean) PluginSettings.ECO_USE_VAULT.get()) && !((boolean) PluginSettings.ECO_HOOK_INTO_VAULT.get()) && !(SpleefX.getPlugin().getVaultHandler().getEconomy() instanceof Economy_SpleefX))
            throw new CommandException("&cVault hook in config is set to true, and the economy is not SpleefX's, hence this command has been disabled. To edit a player's balance, use your standard Vault-based economy plugin.");
        OfflinePlayer target = args.length >= 2 ? Bukkit.getOfflinePlayer(args[1]) : null;
        if (target == null)
            throw new CommandException("&cInvalid player: &e" + target.getName());
        int value = args.length >= 3 ? get(args[2]) : 0;
        switch (args.length) {
            case 1:
                if (sender.hasPermission(BalanceSubcommand.OTHERS)) {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
                    Chat.plugin(sender, "&e" + p.getName() + "&a's money: &e$" + SpleefX.getPlugin().getDataProvider().getStatistics(p).getCoinsFormatted(p));
                } else {
                    if (!(sender instanceof Player))
                        throw new CommandException("&cYou must be a player to use this command!");
                    Chat.plugin(sender, "&eYour money: &a$" + SpleefX.getPlugin().getDataProvider().getStatistics(((Player) sender)).getCoinsFormatted(((Player) sender)));
                }
                break;
            case 2:
                if (args[0].equalsIgnoreCase("reset")) {
                    SpleefX.getPlugin().getDataProvider().getStatistics(target).onCoins(v -> 0);
                    run(sender, target, v -> 0, "&e%p%&a's coins have been set to &e0&a.");
                } else {
                    Chat.plugin(sender, "&cInvalid command usage. Try &e" + getUsage(command) + "&c.");
                }
                break;
            case 3:
                switch (args[0].toLowerCase()) {
                    case "add":
                    case "give":
                        run(sender, target, v -> v + value, "&e%p%&a has been given &e" + value + "&a.");
                        break;
                    case "addboosted":
                        int r = BoosterFactory.boost(target, value);
                        run(sender, target, v -> v + r, "&e%p% &ahas been given a boosted of &e" + r + "&a.");
                        break;
                    case "take":
                    case "remove":
                        run(sender, target, v -> v - value, "&e" + value + "&a has been taken from &e%p%&a.");
                        break;
                    case "set":
                        run(sender, target, v -> value, "&e%p%&a's coins have been set to &e%v%&a.");
                        break;
                }
                break;
        }
        return true;
    }

    private int get(String arg) {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            throw new CommandException("&cInvalid number: &e" + arg + "&c.");
        }
    }

    private void run(CommandSender sender, OfflinePlayer player, IntFunction<Integer> task, String feedback) {
        int i = SpleefX.getPlugin().getDataProvider().getStatistics(player).onCoins(task);
        Chat.plugin(sender, feedback.replace("%p%", player.getName()).replace("%v%", Integer.toString(i)));
    }

    /**
     * Returns a list of tabs for this subcommand.
     *
     * @param sender
     * @param command The command name.
     * @param args    Command arguments. Does <i>NOT</i> contain this subcommand.
     * @return A list of all tabs.
     */
    @Override
    public List<String> onTab(CommandSender sender, Command command, String[] args) {
        if (sender.hasPermission(PERMISSION)) switch (args.length) {
            case 1:
                return TABS.stream().filter(c -> c.startsWith(args[0])).collect(Collectors.toList());
            case 2:
                return null;
            case 3:
                if (args[0].equalsIgnoreCase("reset")) return Collections.emptyList();
                return NUMBERS;
            default:
                return Collections.emptyList();
        }
        else if (sender.hasPermission(BalanceSubcommand.OTHERS)) return null;
        else return Collections.emptyList();
    }
}