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
import io.github.spleefx.data.GameStats;
import io.github.spleefx.economy.booster.BoosterFactory;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.plugin.PluginSettings;
import net.milkbowl.vault.economy.plugins.Economy_SpleefX;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.function.IntFunction;

@PluginSubcommand(
        name = "coins",
        description = "Manage a player's coins",
        parameters = "(add / addboosted / take / set / reset) <player> <amount]",
        permission = "spleefx.admin.coins",
        helpMenu = {
                "&ecoins add <player> <amount> &7- &dGive the specified amount of coins to the player",
                "&ecoins addboosted <player> <amount> &7- &dGive the specified amount of coins to the player, while taking all the active player boosters into consideration",
                "&ecoins take <player> <amount> &7- &dTake the specified amount of coins from the player",
                "&ecoins set <player> <amount> &7- &dSet the amount of coins of the player",
                "&ecoins reset <player> &7- &dReset the player's balance"
        },
        minimumArguments = 2,
        tabCompletions = "add|addboosted|take|set|reset @players @numbers"
)
public class CoinsCommand implements CommandCallback {

    /**
     * Invoked when the command is processed
     *
     * @param context Context of the command (data)
     */
    @Override
    public void onProcess(CommandContext context) {
        if (GameStats.VAULT_EXISTS.get() && ((boolean) PluginSettings.ECO_USE_VAULT.get()) && !((boolean) PluginSettings.ECO_HOOK_INTO_VAULT.get()) && !(SpleefX.getPlugin().getVaultHandler().getEconomy() instanceof Economy_SpleefX))
            throw new CommandCallbackException("&cVault hook in config is set to true, and the economy is not SpleefX's, hence this command has been disabled. To edit a player's balance, use your standard Vault-based economy plugin.");
        String[] args = context.getArgs();
        CommandSender sender = context.getSender();
        OfflinePlayer target = context.resolve(1, OfflinePlayer.class);
        int value = args.length >= 3 ? context.resolve(2, int.class) : 0;
        switch (args.length) {
            case 2:
                if (args[0].equalsIgnoreCase("reset")) {
                    SpleefX.getPlugin().getDataProvider().getStatistics(target).onCoins(v -> 0);
                    run(context.getSender(), target, v -> 0, "&e%p%&a's coins have been set to &e0&a.");
                } else context.invalidUsage();
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
                    default:
                        context.invalidUsage();
                }
        }
    }

    private void run(CommandSender sender, OfflinePlayer player, IntFunction<Integer> task, String feedback) {
        int i = SpleefX.getPlugin().getDataProvider().getStatistics(player).onCoins(task);
        Chat.plugin(sender, feedback.replace("%p%", player.getName()).replace("%v%", Integer.toString(i)));
    }
}
