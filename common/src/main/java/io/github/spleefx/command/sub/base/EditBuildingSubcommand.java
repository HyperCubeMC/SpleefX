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

import com.sk89q.worldedit.EmptyClipboardException;
import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.api.ArenaData;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.command.sub.PluginSubcommand;
import io.github.spleefx.compatibility.worldedit.SchematicManager;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.util.PlaceholderUtil.CommandEntry;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.io.CopyStore;
import io.github.spleefx.util.message.message.Message;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EditBuildingSubcommand extends PluginSubcommand {

    public EditBuildingSubcommand() {
        super("editbuilding", c -> new Permission("spleefx." + c.getName() + ".editbuilding"), "Edit the building of an arena", c -> "/" + c.getName() + " editbuilding <arena>");
    }

    /**
     * Handles the command logic
     *
     * @param command The bukkit command
     * @param sender  Sender of the command
     * @param args    Command arguments
     * @return {@code false} if it is desired to send the help menu, true if otherwise.
     */
    @Override public boolean handle(Command command, CommandSender sender, String[] args) {
        GameExtension extension = ExtensionsManager.getFromCommand(command.getName());
        if (args.length < 1) {
            Chat.prefix(sender, extension, "&cInvalid usage. Try &e" + getUsage(command) + "&c.");
            return true;
        }
        if (checkSender(sender)) {
            Message.NOT_PLAYER.reply(sender, extension);
            return true;
        }
        GameArena arena = GameArena.getByKey(args[0]);
        if (arena == null) {
            Message.INVALID_ARENA.reply(sender, extension, new CommandEntry(command.getName(), args[1]));
            return true;
        }
        rewrite((Player) sender, arena);
        return true;
    }

    public void rewrite(Player player, GameArena arena) {
        try {
            SchematicManager processor = SpleefX.newSchematicManager(arena.getKey());
            processor.write(SpleefX.getPlugin().getWorldEdit().getSession(player).getClipboard());
            Location old = CopyStore.LOCATIONS.get(player);
            if (old == null) throw new EmptyClipboardException();
            arena.setRegenerationPoint(old);
            Chat.prefix(player, arena, "&aSuccessfully overrided schematic building for arena &e" + arena.getKey() + "&a.");
        } catch (EmptyClipboardException e) {
            Chat.prefix(player, arena, "&cYou must select and copy the arena to your clipboard (with WorldEdit)!");
            GameArena.ARENAS.get().remove(arena.getKey());
        }
    }

    /**
     * Returns a list of tabs for this subcommand.
     *
     * @param args Command arguments. Does <i>NOT</i> channelTo this subcommand.
     * @return A list of all tabs.
     */
    @Override
    public List<String> onTab(CommandSender sender, Command command, String[] args) {
        GameExtension extension = ExtensionsManager.getFromCommand(command.getName());
        if (args.length == 1) {
            return GameArena.ARENAS.get().values().stream().filter(gameArena -> gameArena.getKey().startsWith(args[0]) &&
                    extension.getKey().equals(gameArena.getExtension().getKey())).map(ArenaData::getKey).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}