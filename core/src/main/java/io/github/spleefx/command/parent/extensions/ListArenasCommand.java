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

import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.command.core.CommandCallback;
import io.github.spleefx.command.core.CommandContext;
import io.github.spleefx.command.core.PluginSubcommand;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.compatibility.chat.ChatComponent;
import io.github.spleefx.compatibility.chat.ChatEvents.ClickEvent;
import io.github.spleefx.compatibility.chat.ChatEvents.HoverEvent;
import io.github.spleefx.compatibility.chat.ComponentJSON;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.util.game.Chat;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;
import java.util.stream.Collectors;

@PluginSubcommand(
        name = "listarenas",
        permission = "spleefx.{ext}.listarenas",
        permissionAccess = PermissionDefault.TRUE,
        description = "List all arenas of a specific type",
        helpMenu = {"&elistarenas &7- &dList all arenas of this mode"},
        requirePlayer = true,
        aliases = {"list"}
)
public class ListArenasCommand implements CommandCallback {

    private static final ChatComponent DASH = new ChatComponent().setText("&7-", false);

    /**
     * Invoked when the command is processed
     *
     * @param context Context of the command (data)
     */
    @Override
    public void onProcess(CommandContext context) {
        List<GameArena> arenas = GameArena.ARENAS.get().values().stream().filter(e -> e.getExtension().equals(context.getExtension())).collect(Collectors.toList());
        if (arenas.isEmpty()) {
            MessageKey.NO_ARENAS.send(context.getSender(), null, null, null, null, context.getCommand().getName(), null, -1, context.getExtension());
            return;
        }
        if (context.getSender().hasPermission("spleefx.admin")) {
            Command command = context.getCommand();
            arenas.forEach(arena -> {
                ComponentJSON json = new ComponentJSON();
                json
                        .append(new ChatComponent().setText("&e" + arena.getKey() + " &7- " + arena.getEngine().getArenaStage().getState() + " &7- ", false)).space()
                        .append(new ChatComponent()
                                .setText("&7[&6Join&7]", false)
                                .setHoverAction(HoverEvent.SHOW_TEXT, "&eClick to join the arena")
                                .setClickAction(ClickEvent.RUN_COMMAND, "/" + command.getName() + " join " + arena.getKey()))
                        .space().append(DASH).space()
                        .append(new ChatComponent()
                                .setText("&7[&bSettings&7]", false)
                                .setHoverAction(HoverEvent.SHOW_TEXT, "&eClick to open the settings GUI")
                                .setClickAction(ClickEvent.RUN_COMMAND, "/" + command.getName() + " arena settings " + arena.getKey()))
                        .space().append(DASH).space()
                        .append(new ChatComponent()
                                .setText("&7[&aRegenerate&7]", false)
                                .setHoverAction(HoverEvent.SHOW_TEXT, "&eClick to regenerate the arena")
                                .setClickAction(ClickEvent.RUN_COMMAND, "/" + command.getName() + " arena regenerate " + arena.getKey()))
                        .space().append(DASH).space()
                        .append(new ChatComponent()
                                .setText("&7[&cRemove&7]", false)
                                .setHoverAction(HoverEvent.SHOW_TEXT, "&eClick to remove the arena")
                                .setClickAction(ClickEvent.RUN_COMMAND, "/" + command.getName() + " arena remove " + arena.getKey()));
                CompatibilityHandler.getProtocol().send(json, context.getSender());
            });
            return;
        }
        if (!arenas.isEmpty()) {

            arenas.forEach(arena -> {
                ComponentJSON json = new ComponentJSON();
                json.append(new ChatComponent().setText("&e" + arena.getDisplayName() + " &7- " + arena.getEngine().getArenaStage().getState(), false));
                if (context.getSender() instanceof Player) {
                    json.space()
                            .append(new ChatComponent()
                                    .setText(" &7- " + "&7[&6Join&7]", false)
                                    .setHoverAction(HoverEvent.SHOW_TEXT, "&eClick to join the arena")
                                    .setClickAction(ClickEvent.RUN_COMMAND, "/" + context.getCommand().getName() + " join " + arena.getKey()));
                    CompatibilityHandler.getProtocol().send(json, context.getSender());
                } else {
                    Chat.sendUnprefixed(context.getSender(), json.getStripped());
                }
            });
        }
    }
}
