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

import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.ArenaStage;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.command.core.CommandCallback;
import io.github.spleefx.command.core.CommandContext;
import io.github.spleefx.command.core.PluginSubcommand;
import io.github.spleefx.message.MessageKey;
import org.bukkit.permissions.PermissionDefault;

@PluginSubcommand(
        name = "leave",
        permission = "spleefx.{ext}.leave",
        permissionAccess = PermissionDefault.TRUE,
        description = "Leave the current arena",
        requirePlayer = true,
        helpMenu = {"&eleave &7- &dLeave the current arena"}
)
public class LeaveCommand implements CommandCallback {

    /**
     * Invoked when the command is processed
     *
     * @param context Context of the command (data)
     */
    @Override
    public void onProcess(CommandContext context) {
        ArenaPlayer player = ArenaPlayer.adapt(context.player());
        if (player.getCurrentArena() == null) {
            MessageKey.NOT_IN_ARENA.send(context.getSender(), null, null, null, null, context.getCommand().getName(), null, -1, context.getExtension());
            return;
        }
        GameArena arena = player.getCurrentArena();
        ArenaStage stage = arena.getEngine().getArenaStage();
        if ((stage == ArenaStage.COUNTDOWN) || ((stage == ArenaStage.WAITING) && arena.getEngine().getPlayerTeams().containsKey(player)))
            arena.getEngine().quit(player);
        else if (arena.getEngine().getArenaStage() == ArenaStage.ACTIVE)
            arena.getEngine().lose(player, arena.getEngine().getPlayerTeams().get(player));

    }
}
