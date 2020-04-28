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
import io.github.spleefx.arena.api.BaseArenaEngine;
import io.github.spleefx.command.core.CommandCallback;
import io.github.spleefx.command.core.CommandContext;
import io.github.spleefx.command.core.PluginSubcommand;

@PluginSubcommand(
        name = "forcestart",
        description = "Force start the current arena",
        aliases = {"force", "start"},
        permission = "spleefx.{ext}.forcestart",
        requirePlayer = true
)
public class ForceStartCommand implements CommandCallback {

    /**
     * Invoked when the command is processed
     *
     * @param context Context of the command (data)
     */
    @Override
    public void onProcess(CommandContext context) {
        ArenaPlayer player = ArenaPlayer.adapt(context.player());
        ((BaseArenaEngine<?>) player.getCurrentArena().getEngine()).countdown = 1;
        context.reply("&aForcibly starting arena!");
    }
}
