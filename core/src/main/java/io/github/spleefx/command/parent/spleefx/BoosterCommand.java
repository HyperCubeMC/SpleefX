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
import io.github.spleefx.economy.booster.BoosterFactory;
import io.github.spleefx.economy.booster.BoosterMenu;
import io.github.spleefx.util.game.Chat;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;

@PluginSubcommand(
        name = "boosters",
        description = "Manage boosters",
        parameters = "add &b<player> &d<booster>",
        permission = "spleefx.admin.boosters",
        tabCompletions = "add @players",
        helpMenu = {"&eboosters add <player> <booster type> &7- &dGive the specified booster type to the player"}
)
public class BoosterCommand implements CommandCallback {

    /**
     * Invoked when the command is processed
     *
     * @param context Context of the command (data)
     */
    @Override
    public void onProcess(CommandContext context) {
        String[] args = context.getArgs();
        if (args.length == 0) {
            context.player().openInventory(new BoosterMenu(new ArrayList<>(SpleefX.getPlugin().getDataProvider().getStatistics(context.player())
                    .getBoosters().values())).createInventory());
            return;
        }
        context.requireArgs(3);
        if (args[0].equalsIgnoreCase("add")) {
            OfflinePlayer target = context.resolve(1, OfflinePlayer.class);
            BoosterFactory factory = context.resolve(2, BoosterFactory.class);
            factory.give(target);
            if (target.isOnline())
                Chat.plugin(target.getPlayer(), "&aYou have been given a booster of type &e" + factory.getDisplayName() + "&a.");
        }
    }
}
