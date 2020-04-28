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
package io.github.spleefx.command.parent.extensions;

import io.github.spleefx.SpleefX;
import io.github.spleefx.command.core.CommandCallback;
import io.github.spleefx.command.core.CommandContext;
import io.github.spleefx.command.core.PluginSubcommand;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.game.Metas;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.PermissionDefault;

@PluginSubcommand(
        name = "stats",
        permission = "spleefx.{ext}.stats",
        permissionAccess = PermissionDefault.TRUE,
        description = "Display the statistics GUI",
        helpMenu = {"&estats &a[player=you] &7- &dDisplay your/other players' statistics."},
        aliases = "statistics",
        tabCompletions = "@stats @stats"
)
public class StatsCommand implements CommandCallback {

    private static final MetadataValue VIEWING = new FixedMetadataValue(SpleefX.getPlugin(), true);

    /**
     * Invoked when the command is processed
     *
     * @param context Context of the command (data)
     */
    @Override
    public void onProcess(CommandContext context) {
        String[] args = context.getArgs();
        GameExtension e = context.getExtension();
        CommandSender sender = context.getSender();
        Command command = context.getCommand();
        Player s = context.player();
        switch (args.length) {
            case 0:
                viewSelf(s, e);
                return;
            case 1:
                if (args[0].equalsIgnoreCase("global")) {
                    viewSelf(s, null);
                } else {
                    if (args[0].equals(sender.getName())) {
                        viewSelf(s, e);
                        return;
                    }
                    if (sender.hasPermission("spleefx." + command.getName() + ".stats.others")) {
                        Player player = Bukkit.getPlayer(args[0]);
                        if (player == null) {
                            Chat.prefix(sender, e, MessageKey.UNKNOWN_PLAYER.getText().replace("{player}", args[0]));
                            return;
                        }
                        Inventory inventory = SpleefX.getPlugin().getDataProvider().createGUI(player, e);
                        s.openInventory(inventory);
                        Metas.set(s, "spleefx.viewing_stats", VIEWING);
                    } else {
                        Chat.prefix(sender, e, MessageKey.NO_PERMISSION_STATISTICS.getText());
                    }
                }
                return;
            case 2:
                if (args[1].equalsIgnoreCase("global")) {
                    if (args[0].equals(sender.getName())) {
                        viewSelf(s, e);
                        return;
                    }
                    if (sender.hasPermission("spleefx." + command.getName() + ".stats.others")) {
                        Player player = Bukkit.getPlayer(args[0]);
                        if (player == null) {
                            Chat.prefix(sender, e, MessageKey.UNKNOWN_PLAYER.getText().replace("{player}", args[0]));
                            return;
                        }
                        Inventory inventory = SpleefX.getPlugin().getDataProvider().createGUI(player, null);
                        s.openInventory(inventory);
                        Metas.set(s, "spleefx.viewing_stats", VIEWING);
                    } else {
                        Chat.prefix(sender, e, MessageKey.NO_PERMISSION_STATISTICS.getText());
                    }
                    return;
                }
                break;
        }
    }

    private static void viewSelf(Player player, GameExtension mode) {
        Inventory inventory = SpleefX.getPlugin().getDataProvider().createGUI(player, mode);
        player.openInventory(inventory);
        Metas.set(player, "spleefx.viewing_stats", VIEWING);
    }

    public static class MenuListener implements Listener {

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            event.getPlayer().removeMetadata("spleefx.viewing_stats", SpleefX.getPlugin());
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (event.getWhoClicked().hasMetadata("spleefx.viewing_stats"))
                event.setCancelled(true);
        }
    }
}
