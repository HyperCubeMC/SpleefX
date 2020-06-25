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

import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.ArenaStage;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.command.sub.PluginSubcommand;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.message.MessageKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.Collections;
import java.util.List;

public class LeaveSubcommand extends PluginSubcommand {

    private static final List<String> HELP = Collections.singletonList(
            "&eleave &7- &dLeave the current arena"
    );

    public LeaveSubcommand() {
        super("leave",
                (c) -> new Permission("spleefx." + c.getName() + ".leave", PermissionDefault.TRUE),
                "Leave the current arena",
                (c) -> "/" + c.getName() + " leave");
        this.helpMenu = HELP;
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
        if (!(sender instanceof Player)) {
            MessageKey.NOT_PLAYER.send(sender, null, null, null, null, command.getName(), null, -1, extension);
            return true;
        }
        ArenaPlayer player = ArenaPlayer.adapt((Player) sender);
        if (player.getCurrentArena() == null) {
            MessageKey.NOT_IN_ARENA.send(sender, null, null, null, null, command.getName(), null, -1, extension);
            return true;
        }
        GameArena arena = player.getCurrentArena();
        ArenaStage stage = arena.getEngine().getArenaStage();
        if ((stage == ArenaStage.COUNTDOWN) || ((stage == ArenaStage.WAITING) && arena.getEngine().getPlayerTeams().containsKey(player)))
            arena.getEngine().quit(player, true);
        else if (arena.getEngine().getArenaStage() == ArenaStage.ACTIVE)
            arena.getEngine().lose(player, arena.getEngine().getPlayerTeams().get(player), true);
        return true;
    }
}