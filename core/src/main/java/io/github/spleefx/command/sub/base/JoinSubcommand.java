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
import io.github.spleefx.arena.api.ArenaData;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.command.sub.PluginSubcommand;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.plugin.ArenaSelector;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JoinSubcommand extends PluginSubcommand {

    private static final List<String> HELP = Collections.singletonList(
            "&ejoin &a<arena> &7- &dJoin the specified arena"
    );

    protected Function<Command, GameExtension> extension;

    public JoinSubcommand(Function<Command, GameExtension> extension) {
        super("join",
                (c) -> new Permission("spleefx." + c.getName() + ".join", PermissionDefault.TRUE),
                "Join an arena",
                (c) -> "/" + c.getName() + " join <arena>");
        this.extension = extension;
        super.helpMenu = HELP;
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
        GameExtension mode = extension.apply(command);
        if (checkSender(sender)) {
            MessageKey.NOT_PLAYER.send(sender, null, null, null, null, command.getName(),
                    null, -1, mode);
            return true;
        }
        ArenaPlayer player = ArenaPlayer.adapt(((Player) sender));
        if (args.length == 0) {
            GameArena arena = ArenaSelector.pick(mode);
            if (arena == null) {
                MessageKey.NO_AVAILABLE_ARENA.send(sender, null, null, null, player.getPlayer(), command.getName(),
                        null, -1, mode);
                return true;
            }
            arena.getEngine().join(player, null);
            return true;
        }
        String arenaKey = args[0];
        GameArena arena = GameArena.getByKey(arenaKey); // Get by key
        if (arena == null) arena = GameArena.getByName(arenaKey = combine(args, 0)); // Get by display name
        if (arena == null) {
            Chat.sendUnprefixed(sender, mode.getChatPrefix() + MessageKey.INVALID_ARENA.getText().replace("{arena}", arenaKey));
            return true;
        }
        arena.getEngine().join(player, null);
        return true;
    }

    /**
     * Returns a list of tabs for this subcommand.
     *
     * @param args Command arguments. Does <i>NOT</i> contain this subcommand.
     * @return A list of all tabs.
     */
    @Override
    public List<String> onTab(CommandSender sender, Command command, String[] args) {
        GameExtension extension = this.extension.apply(command);
        if (args.length == 1) {
            return GameArena.ARENAS.get().values().stream().filter(gameArena -> gameArena.getKey().startsWith(args[0]) &&
                    extension.getKey().equals(gameArena.getExtension().getKey())).map(ArenaData::getKey).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}