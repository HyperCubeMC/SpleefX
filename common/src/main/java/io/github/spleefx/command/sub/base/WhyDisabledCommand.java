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

import io.github.spleefx.arena.api.ArenaData;
import io.github.spleefx.arena.api.ArenaType;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.command.sub.PluginSubcommand;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.compatibility.chat.ChatComponent;
import io.github.spleefx.compatibility.chat.ChatEvents.ClickEvent;
import io.github.spleefx.compatibility.chat.ChatEvents.HoverEvent;
import io.github.spleefx.compatibility.chat.ComponentJSON;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.util.PlaceholderUtil.CommandEntry;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.message.message.Message;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class WhyDisabledCommand extends PluginSubcommand {

    private static final Permission PERMISSION = new Permission("spleefx.admin.whydisabled");
    private static final ComponentJSON JSON = new ComponentJSON();
    private static final ChatComponent ARENA_DISABLED = new ChatComponent().setText("&7- &cArena is disabled ", true);

    /**
     * A predicate for testing whether
     */
    public static final Predicate<GameArena> IS_READY = arena -> (arena.getMaxPlayerCount() >= 2) && (arena.getFFAManager().getSpawnpoints().size() >= arena.getMaximum());

    public WhyDisabledCommand() {
        super("whydisabled", c -> PERMISSION, "Get a summary of what's left for the arena setup", c -> "/" + c.getName() + " whydisabled <arena>");
    }

    public static final Function<GameArena, Set<String>> DISABLED = (arena) -> {
        Set<String> violations = new HashSet<>();
        if (!arena.isEnabled()) {
            violations.add("Arena is disabled [Enable]");
        }
        if (arena.getLobby() == null) {
            violations.add("§eNo lobby has been set");
        }
        if (arena.getMinimum() > arena.getMaximum()) {
            violations.add("Arena maximum player count is greater than the minimum count");
        }
        if (arena.getMinimum() < 2) {
            violations.add("Minimum is less than 2 players");
        }
        if (arena.getArenaType() == ArenaType.FREE_FOR_ALL) {
            if ((arena.getMaxPlayerCount() < 2))
                violations.add("Max player count is less than 2");
        }
        Map<?, Location> spawnPoints = arena.getArenaType() == ArenaType.TEAMS ? arena.getSpawnPoints() : arena.getFFAManager().getSpawnpoints();
        if (!(spawnPoints.size() >= arena.getMaximum())) {
            int delta = arena.getMaximum() - spawnPoints.size();
            violations.add("Not all spawn-points have been set §e(§a" + spawnPoints.size() + " §eset, §7" + delta + " §eleft)");
        }
        return violations;
    };

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
        GameArena arena = GameArena.getByKey(args[0]);
        if (arena == null) {
            Message.INVALID_ARENA.reply(sender, extension, new CommandEntry(command.getName(), args[1]));
            return true;
        }
        Set<String> violations = DISABLED.apply(arena);
        if (violations.isEmpty()) {
            Chat.prefix(sender, extension, "&aThis arena is ready, as everything has been set!");
            return true;
        }
        Chat.prefix(sender, extension, "&cViolations:");
        if (violations.remove("Arena is disabled [Enable]")) {
            //violations.remove("Arena is disabled [Enable]");
            JSON.clear()
                    .append(ARENA_DISABLED)
                    .append(new ChatComponent()
                            .setText("&a&l[Enable]", false)
                            .setHoverAction(HoverEvent.SHOW_TEXT, "Click to enable")
                            .setClickAction(ClickEvent.RUN_COMMAND, "/" + command.getName() + " arena settings " + arena.getKey() + " enable"));
            CompatibilityHandler.getProtocol().send(JSON, sender);
            JSON.clear();
        }
        violations.forEach(v -> Chat.prefix(sender, extension, "&7- &c" + v));

        return true;
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
