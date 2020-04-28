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
import org.bukkit.OfflinePlayer;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

@PluginSubcommand(
        name = "balance",
        description = "Get your/someone's balance",
        parameters = "[player]",
        tabCompletions = "@players",
        permission = "spleefx.balance",
        permissionAccess = PermissionDefault.TRUE,
        aliases = {"bal", "money"}
)
public class BalanceCommand implements CommandCallback {

    protected static final Permission OTHERS = new Permission("spleefx.balance.others", PermissionDefault.TRUE);

    /**
     * Invoked when the command is processed
     *
     * @param context Context of the command (data)
     */
    @Override
    public void onProcess(CommandContext context) {
        String[] args = context.getArgs();
        if (args.length == 0) {
            context.reply("&eYour money: &a$" + SpleefX.getPlugin().getDataProvider().getStatistics(context.player()).getCoinsFormatted(context.player()));
            return;
        }
        OfflinePlayer p = context.resolve(0, OfflinePlayer.class);
        if (context.getSender().hasPermission(OTHERS))
            context.reply("&e" + p.getName() + "&a's money: &e$" + SpleefX.getPlugin().getDataProvider().getStatistics(p).getCoinsFormatted(p));
        else
            context.reply("&eYour money: &a$" + SpleefX.getPlugin().getDataProvider().getStatistics(context.player()).getCoinsFormatted(context.player()));
    }
}