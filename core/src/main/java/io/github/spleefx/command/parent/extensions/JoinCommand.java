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
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.command.core.CommandCallback;
import io.github.spleefx.command.core.CommandContext;
import io.github.spleefx.command.core.PluginSubcommand;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.util.plugin.ArenaSelector;
import org.bukkit.permissions.PermissionDefault;

@PluginSubcommand(
        name = "join",
        permission = "spleefx.{ext}.join",
        permissionAccess = PermissionDefault.TRUE,
        description = "Join an arena",
        parameters = "<arena>",
        requirePlayer = true,
        tabCompletions = "@arenas",
        helpMenu = {"&ejoin &a<arena> &7- &dJoin the specified arena"}
)
public class JoinCommand implements CommandCallback {

    /**
     * Invoked when the command is processed
     *
     * @param context Context of the command (data)
     */
    @Override
    public void onProcess(CommandContext context) {
        String[] args = context.getArgs();
        ArenaPlayer player = ArenaPlayer.adapt(context.player());
        if (args.length == 0) {
            GameArena arena = ArenaSelector.pick(context.getExtension());
            if (arena == null) {
                MessageKey.NO_AVAILABLE_ARENA.send(context.getSender(), null, null, null, player.getPlayer(), context.getCommand().getName(),
                        null, -1, context.getExtension());
                return;
            }
            arena.getEngine().join(player, null);
            return;
        }
        GameArena arena = context.resolve(0, GameArena.class);
        arena.getEngine().join(player, null);
    }
}
