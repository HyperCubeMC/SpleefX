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
import io.github.spleefx.perk.PerkShop.ShopMenu;
import io.github.spleefx.util.message.message.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class PerksSubcommand extends PluginSubcommand {

    private static final Permission PERMISSION = new Permission("spleefx.perks.shop", PermissionDefault.TRUE);

    public PerksSubcommand() {
        super("perks", (c) -> PERMISSION, "Open the perks shop GUI", (c) -> "/spleefx perks");
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
        if (!(sender instanceof Player)) {
            Message.NOT_PLAYER.reply(sender);
            return true;
        }
        Player player = ((Player) sender);
        new ShopMenu(SpleefX.getPerkShop()).display(player);
        return true;
    }
}
