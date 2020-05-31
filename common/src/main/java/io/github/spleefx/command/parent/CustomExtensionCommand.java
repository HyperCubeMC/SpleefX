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
package io.github.spleefx.command.parent;

import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.ModeType;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.arena.custom.ExtensionArena;
import io.github.spleefx.command.sub.CommandManager;
import io.github.spleefx.command.sub.base.*;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.message.MessageKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.spleefx.command.parent.CommandSpleef.HELP;

/**
 * A simple callback class to handle any custom-extension command. This should <i>NOT</i> be registered
 * in {@link SpleefX#onEnable}.
 */
public class CustomExtensionCommand implements TabExecutor {

    public static final CustomExtensionCommand INSTANCE = new CustomExtensionCommand();

    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0)
            args = HELP;
        if (!CommandManager.CUSTOM.runSub(command, sender, args))
            MessageKey.UNKNOWN_SUBCOMMAND.send(sender, null, null, null, null,
                    command.getName(), null, -1, ExtensionsManager.getFromCommand(command.getName()));
        return false;
    }

    /**
     * Requests a list of possible completions for a command argument.
     *
     * @param sender  Source of the command.  For players tab-completing a
     *                command inside of a command block, this will be the player, not
     *                the command block.
     * @param command Command which was executed
     * @param alias   The alias used
     * @param args    The arguments passed to the command, including final
     *                partial argument to be completed and command label
     * @return A List of possible completions for the final argument, or null
     * to default to the command executor
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return CommandSpleefX.onTab(sender, command, args, CommandManager.CUSTOM);
    }

    static {
        JoinSubcommand joinSubcommand = new JoinSubcommand(c -> ExtensionsManager.getFromCommand(c.getName())) {
            public List<String> onTab(CommandSender sender, Command command, String[] args) {
                return GameArena.ARENAS.get().entrySet().stream().filter(entry -> entry.getKey().startsWith(args[0]) && entry.getValue().getExtension().equals(ExtensionsManager.getFromCommand(command.getName()))).collect(Collectors.toList()).stream().map(Map.Entry::getKey).collect(Collectors.toList());
            }
        };
        ArenaSubcommand<ExtensionArena> arenaSubcommand = new ArenaSubcommand<>(ModeType.CUSTOM, ((key, displayName, regenerationPoint, arenaType, extensionMode) -> new ExtensionArena(key, displayName, regenerationPoint, extensionMode, arenaType)));
        CommandManager.CUSTOM.registerCommand(
                new StatsCommand(),
                joinSubcommand,
                new ForceStartCommand(),
                new WhyDisabledCommand(),
                new JoinGUISubcommand(), new EditBuildingSubcommand(),
                new LeaveSubcommand(),
                new ListArenasCommand(command -> GameArena.ARENAS.get().values().stream().filter(v -> v.getExtension().equals(ExtensionsManager.getFromCommand(command.getName()))).collect(Collectors.toList())),
                arenaSubcommand,
                new HelpSubcommand(CommandManager.CUSTOM));
    }
/*

    private static List<String> onTab(final Command command, final String[] args) {
        switch (args.length) {
            case 1: {
                return ArenaSubcommand.ARGS_1.stream().filter(a -> a.startsWith(args[0])).collect(Collectors.toList());
            }
            case 2: {
                if (args[0].equalsIgnoreCase("create")) return Collections.emptyList();
                return GameArena.ARENAS.get().values().stream().filter(arena -> arena.type == ModeType.CUSTOM && arena.getKey().startsWith(args[1]) && arena.getExtension().equals(ExtensionsManager.getFromCommand(command.getName()))).map(ArenaData::getKey).collect(Collectors.toList());
            }
            case 3: {
                if (args[0].equalsIgnoreCase("create"))
                    return ArenaSubcommand.TYPES.stream().filter(s -> s.startsWith(args[2])).collect(Collectors.toList());
                if (!args[0].equalsIgnoreCase("spawnpoint")) break;
                final GameArena arena2 = GameArena.getByKey(args[1]);
                if (arena2 == null) return Collections.emptyList();
                return arena2.getTeams().stream().map(team -> team.getName().toLowerCase()).collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
*/

}