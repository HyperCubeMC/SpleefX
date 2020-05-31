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
import io.github.spleefx.gui.MessageGUI;
import io.github.spleefx.message.MessageCategory;
import io.github.spleefx.message.MessageKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.permissions.Permission;

import java.util.Collections;
import java.util.List;

public class MessagesSubcommand extends PluginSubcommand {

    private static final List<String> HELP = Collections.singletonList(
            "&emessages &7- &dOpen the messages GUI"
    );

    private static final Permission PERMISSION = new Permission("spleefx.admin.messages");

    public MessagesSubcommand() {
        super("messages", (c) -> PERMISSION, "Displays the message editing GUI", (c) -> "/spleefx messages");
        super.helpMenu = HELP;
        super.aliases = Collections.singletonList("msgs");
    }

    /**
     * Handles the command input
     *
     * @param sender Command sender
     * @param args   Extra command arguments
     * @return {@code true} if the command succeed, {@code false} if it is desired to send {@link #getHelpMenu()}.
     */
    @Override
    public boolean handle(Command command, CommandSender sender, String[] args) {
        if (checkSender(sender)) {
            MessageKey.NOT_PLAYER.send(sender, null, null, null, null, command.getName(), null, -1, null);
            return true;
        }
        Inventory i = new MessageGUI(MessageCategory.VALUES[0]).createInventory();
        ((HumanEntity) sender).openInventory(i);
        return true;
    }
}