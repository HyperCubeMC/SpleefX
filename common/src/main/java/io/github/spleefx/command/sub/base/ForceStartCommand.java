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
package io.github.spleefx.command.sub.base;

import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.ArenaStage;
import io.github.spleefx.arena.api.BaseArenaEngine;
import io.github.spleefx.command.sub.PluginSubcommand;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.util.game.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public class ForceStartCommand extends PluginSubcommand {

    private static final Permission PERMISSION = new Permission("spleefx.admin.forcestart");

    public ForceStartCommand() {
        super("forcestart", c -> PERMISSION, "Forcibly start the arena", c -> "/" + c.getName() + " forcestart");
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
        GameExtension extension = ExtensionsManager.getFromCommand(command.getName());
        if (checkSender(sender)) {
            MessageKey.NOT_PLAYER.send(sender, null, null, null, null, command.getName(),
                    null, -1, extension);
            return true;
        }
        ArenaPlayer player = ArenaPlayer.adapt(((Player) sender));
        if (player.getCurrentArena() == null) {
            Chat.prefix(sender, extension, "&cYou must be in an arena!");
            return true;
        }
        if (player.getCurrentArena().getEngine().getArenaStage() != ArenaStage.COUNTDOWN) {
            Chat.prefix(sender, extension, "&cThe arena hasn't started countdown yet!");
            return true;
        }
        Chat.prefix(sender, extension, "&aForcibly starting arena!");
        ((BaseArenaEngine<?>) player.getCurrentArena().getEngine()).countdown = 1;
        return true;
    }
}
