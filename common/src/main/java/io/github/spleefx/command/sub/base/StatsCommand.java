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
package io.github.spleefx.command.sub.base;

import io.github.spleefx.SpleefX;
import io.github.spleefx.command.sub.PluginSubcommand;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.game.Metas;
import io.github.spleefx.util.message.message.Message;
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
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("JavaDoc")
public class StatsCommand extends PluginSubcommand {

    private static final List<String> HELP = Collections.singletonList(
            "&estats &a[player] &7- &dDisplay your/other players' statistics."
    );

    private static final List<String> ALIASES = Collections.singletonList("statistics");

    private static final List<String> TABS = Collections.singletonList("global");

    private static GameExtension getExtension(Command command) {
        return ExtensionsManager.getFromCommand(command.getName());
    }

    private static final MetadataValue VIEWING = new FixedMetadataValue(SpleefX.getPlugin(), true);

    public StatsCommand() {
        super(
                "stats",
                (c) -> new Permission("spleefx." + c.getName() + ".stats", PermissionDefault.TRUE),
                "Display player statistics",
                (c) -> "/" + c.getName() + " stats [player]");
        super.aliases = ALIASES;
        helpMenu = HELP;

    }

    /**
     * Handles the command input
     *
     * @param command
     * @param sender  Command sender
     * @param args    Extra command arguments
     * @return {@code true} if the command succeed, {@code false} if it is desired to send {@link #getHelpMenu()}.
     */
    @Override
    public boolean handle(Command command, CommandSender sender, String[] args) {
        GameExtension e = getExtension(command);
        if (e == null) {
            Chat.plugin(sender, "&cThis extension is not loaded (Does it have any arenas?)");
            return true;
        }
        if (sender instanceof Player) {
            Player s = (Player) sender;
            switch (args.length) {
                case 0:
                    viewSelf(s, e);
                    return true;
                case 1:
                    if (args[0].equalsIgnoreCase("global")) {
                        viewSelf(s, null);
                    } else {
                        if (args[0].equals(sender.getName())) {
                            viewSelf(s, e);
                            return true;
                        }
                        if (sender.hasPermission("spleefx." + command.getName() + ".stats.others")) {
                            Player player = Bukkit.getPlayer(args[0]);
                            if (player == null) {
                                Chat.sendUnprefixed(sender, Message.UNKNOWN_PLAYER.create(e).replace("{player}", args[0]));
                                return true;
                            }
                            Inventory inventory = SpleefX.getPlugin().getDataProvider().createGUI(player, e);
                            s.openInventory(inventory);
                            Metas.set(s, "spleefx.viewing_stats", VIEWING);
                        } else {
                            Message.NO_PERMISSION_STATISTICS.reply(sender, e);
                        }
                    }
                    return true;
                case 2:
                    if (args[1].equalsIgnoreCase("global")) {
                        if (args[0].equals(sender.getName())) {
                            viewSelf(s, e);
                            return true;
                        }
                        if (sender.hasPermission("spleefx." + command.getName() + ".stats.others")) {
                            Player player = Bukkit.getPlayer(args[0]);
                            if (player == null) {
                                Chat.prefix(sender, e, Message.UNKNOWN_PLAYER.create().replace("{player}", args[0]));
                                return true;
                            }
                            Inventory inventory = SpleefX.getPlugin().getDataProvider().createGUI(player, null);
                            s.openInventory(inventory);
                            Metas.set(s, "spleefx.viewing_stats", VIEWING);
                        } else {
                            Chat.prefix(sender, e, Message.NO_PERMISSION_STATISTICS.create());
                        }
                        return true;
                    }
                    break;
            }
        } else {
            Chat.prefix(sender, e, "&cYou must be a player to use this command!");
        }
        return true;
    }

    /**
     * Returns a list of tabs for this subcommand.
     *
     * @param command The command name.
     * @param args    Command arguments. Does <i>NOT</i> channelTo this subcommand.
     * @return A list of all tabs.
     */
    @Override
    public List<String> onTab(CommandSender sender, Command command, String[] args) {
        return args.length == 2 ? TABS : args.length == 1 ? (!args[0].isEmpty() && "global".startsWith(args[0]) ? TABS : null) : Collections.emptyList();
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