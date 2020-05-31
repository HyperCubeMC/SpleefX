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

import io.github.spleefx.arena.ModeType;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.command.sub.PluginSubcommand;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.compatibility.chat.ChatComponent;
import io.github.spleefx.compatibility.chat.ChatEvents.ClickEvent;
import io.github.spleefx.compatibility.chat.ChatEvents.HoverEvent;
import io.github.spleefx.compatibility.chat.ComponentJSON;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.util.game.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListArenasCommand extends PluginSubcommand {

    private static final List<String> HELP = Collections.singletonList(
            "&elistarenas &7- &dList all arenas of this mode"
    );

    private Function<Command, List<GameArena>> arenas;

    public ListArenasCommand(Function<Command, List<GameArena>> arenas) {
        super(
                "listarenas",
                (c) -> new Permission("spleefx." + c.getName() + ".listarenas", PermissionDefault.TRUE),
                "List all arenas of a specific type", (c) -> "/" + c.getName() + " listarenas");
        this.arenas = arenas;
        super.aliases = Collections.singletonList("list");
        this.helpMenu = HELP;
    }

    private static final ChatComponent DASH = new ChatComponent().setText("&7-", false);

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
        GameExtension e = ExtensionsManager.getFromCommand(command.getName());
        List<GameArena> arenas = this.arenas.apply(command);
        if (arenas.isEmpty()) {
            MessageKey.NO_ARENAS.send(sender, null, null, null, null, command.getName(), null, -1, e);
            return true;
        }
        if (sender.hasPermission("spleefx.admin")) {
            if (sender instanceof Player) {
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
                    CompatibilityHandler.getProtocol().send(json, sender);
                });
                return true;
            }
        }
        if (!arenas.isEmpty()) {

            arenas.forEach(arena -> {
                ComponentJSON json = new ComponentJSON();
                json.append(new ChatComponent().setText("&e" + arena.getDisplayName() + " &7- " + arena.getEngine().getArenaStage().getState(), false));
                if (sender instanceof Player) {
                    json.space()
                            .append(new ChatComponent()
                                    .setText(" &7- " + "&7[&6Join&7]", false)
                                    .setHoverAction(HoverEvent.SHOW_TEXT, "&eClick to join the arena")
                                    .setClickAction(ClickEvent.RUN_COMMAND, "/" + command.getName() + " join " + arena.getKey()));
                    CompatibilityHandler.getProtocol().send(json, sender);
                } else {
                    Chat.sendUnprefixed(sender, json.getStripped());
                }
            });
        }
        return true;
    }

    public static List<GameArena> fromType(ModeType type) {
        return GameArena.ARENAS.get().values().stream().filter(arena -> arena.type == type).collect(Collectors.toList());
    }

}
