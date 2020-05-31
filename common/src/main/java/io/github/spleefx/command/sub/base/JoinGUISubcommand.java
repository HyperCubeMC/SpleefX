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

import io.github.spleefx.command.sub.PluginSubcommand;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.gui.JoinGUI;
import io.github.spleefx.gui.JoinGUI.MenuSettings;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.util.game.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class JoinGUISubcommand extends PluginSubcommand {

    public JoinGUISubcommand() {
        super("joingui", (c) -> new Permission("spleefx." + c.getName() + ".joingui", PermissionDefault.TRUE),
                "Display the join GUI", (c) -> "/" + c.getName() + " joingui");
    }

    @Override
    public boolean handle(Command command, CommandSender sender, String[] args) {
        GameExtension extension = ExtensionsManager.getFromCommand(command.getName());
        if (!(sender instanceof Player)) {
            Chat.prefix(sender, extension, MessageKey.NOT_PLAYER.getText());
            return true;
        }
        MenuSettings menu = MenuSettings.MENU.get();
        new JoinGUI(menu, ((Player) sender), extension);
        return false;
    }

}