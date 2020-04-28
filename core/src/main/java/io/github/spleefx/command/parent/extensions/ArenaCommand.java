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

import com.google.common.base.Preconditions;
import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.api.ArenaType;
import io.github.spleefx.arena.api.FFAManager;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.command.core.CommandCallback;
import io.github.spleefx.command.core.CommandContext;
import io.github.spleefx.command.core.PluginSubcommand;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.gui.ArenaSettingsGUI;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.team.GameTeam;
import io.github.spleefx.team.TeamColor;
import io.github.spleefx.util.game.Metas;
import io.github.spleefx.util.io.CopyStore;
import io.github.spleefx.util.io.FileManager;
import org.bukkit.Location;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@PluginSubcommand(
        name = "arena",
        description = "Manage arenas",
        parameters = "<...>",
        permission = "spleefx.admin.arena",
        tabCompletions = "create|remove|spawnpoint|settings|lobby|finishingloc|removefinishingloc|removelobby|regenerate @ext_arenas @arenacompl @arenacompl @arenacompl @arenacompl @arenacompl @arenacompl @arenacompl @arenacompl",
        minimumArguments = 2,
        helpMenu = {
                "&earena &ccreate &a<arena key> &3[ffa | teams] &b<arena display name> &7- &dCreate a map with a unique key and a display name",
                "&earena &cremove &a<arena key> &7- &dRemove an arena",
                "&earena &cspawnpoint &a<arena key> &b<team> &7- &dSet the spawnpoint of a specific team",
                "&earena &csettings &a<arena key> &7- &dOpen the arena settings GUI",
                "&earena &clobby &a<arena key> [index/team] &7- &dSet the arena lobby (waiting area). No arguments will set the global lobby",
                "&earena &cfinishingloc &a<arena key> &7- &dSet the arena's finishing location, where players are teleported when the game is over.",
                "&earena &cremovefinishingloc &a<arena key> &7- &dRemove the arena's finishing location",
                "&earena &cremovelobby &a<arena key> &7- &dRemove the arena lobby",
                "&earena &cregenerate &a<arena key> &7- &dRegenerate the arena"
        }
)
public class ArenaCommand<R extends GameArena> implements CommandCallback {

    private ArenaFactory<R> arenaFactory;

    public ArenaCommand(ArenaFactory<R> arenaFactory) {
        this.arenaFactory = arenaFactory;
    }

    /**
     * Invoked when the command is processed
     *
     * @param context Context of the command (data)
     */
    @Override
    public void onProcess(CommandContext context) {
        GameExtension ex = Preconditions.checkNotNull(context.getExtension(), "Extension");
        String[] args = context.getArgs();
        final GameArena arena = !args[0].equals("create") ? context.resolve(1, GameArena.class) : null;
        switch (args.length) {
            case 2:
                switch (args[0]) {
                    case "remove":
                        try {
                            R removed = (R) SpleefX.getPlugin().getArenaManager().removeArena(args[1]);
                            if (removed == null)
                                context.reply(MessageKey.INVALID_ARENA.getText().replace("{arena}", args[1]));
                            else
                                MessageKey.ARENA_DELETED.send(context.getSender(), removed, null, null, null, context.getCommand().getName(),
                                        null, -1, ex);
                        } catch (IllegalStateException e) {
                            context.reply("&c" + e.getMessage());
                        }
                        return;
                    case "lobby": {
                        Location old = context.player().getLocation();
                        Location lobby = arena.setLobby(new Location(old.getWorld(), old.getBlockX() + 0.5, old.getBlockY(), old.getBlockZ() + 0.5, old.getYaw(), old.getPitch()));
                        MessageKey.LOBBY_SET.send(context.getSender(), arena, null, lobby, null,
                                context.getCommand().getName(), null, -1, ex);
                        return;
                    }
                    case "removelobby": {
                        arena.setLobby(null);
                        context.reply("&aLobby for arena &e" + arena.getKey() + " &ahas been removed.");
                        return;
                    }
                    case "finishingloc": {
                        Location old = context.player().getLocation();
                        Location loc = arena.setFinishingLocation(new Location(old.getWorld(), old.getBlockX() + 0.5, old.getBlockY(), old.getBlockZ() + 0.5, old.getYaw(), old.getPitch()));
                        context.reply(String.format("&aArena &e%s&a's finishing location has been set to &e%.1f&a, &e%.1f&a, &e%.1f&a.", arena.getKey(), loc.getX(), loc.getY(), loc.getZ()));
                        return;
                    }
                    case "removefinishingloc": {
                        arena.setFinishingLocation(null);
                        context.reply("&aFinishing location for arena &e" + arena.getKey() + " &ahas been removed.");
                        return;
                    }
                    case "regen":
                    case "regenerate": {
                        context.reply("&eRegenerating...");
                        arena.getEngine().regenerate();
                        context.reply("&aArena &e" + arena.getKey() + " &ahas been regenerated.");
                        return;
                    }
                    case "settings": {
                        Metas.set(context.player(), "spleefx.editing", new FixedMetadataValue(SpleefX.getPlugin(), arena));
                        new ArenaSettingsGUI(arena, context.player());
                    }
                }
                return;
            case 3:
                switch (args[0]) {
                    case "settings": {
                        switch (args[2].toLowerCase()) {
                            case "toggle":
                                arena.setEnabled(!arena.isEnabled());
                                context.reply(String.format(arena.isEnabled() ? "&aArena &e%s &ahas been enabled" : "&cArena &e%s &chas been disabled", arena.getKey()));
                                return;
                            case "enable":
                                arena.setEnabled(true);
                                context.reply("&aArena &e" + arena.getKey() + " &ahas been enabled");
                                return;
                            case "disable":
                                arena.setEnabled(false);
                                context.reply("&cArena &e" + arena.getKey() + " &chas been disabled");
                                return;
                            default:
                                context.invalidUsage();
                                return;
                        }
                    }
                    case "spawnpoint": {
                        if (arena.getArenaType() == ArenaType.FREE_FOR_ALL) {
                            FFAManager m = arena.getFFAManager();
                            int index = context.resolve(2, int.class);
                            if (index > arena.getMaxPlayerCount()) {
                                throw new CommandCallbackException(String.format("&cValue &e%s &cis greater than the arena's maximum count (&e%s&c)", index, arena.getMaxPlayerCount()));
                            }
                            Location old = context.player().getLocation();
                            Location spawn = new Location(old.getWorld(), old.getBlockX() + 0.5, old.getBlockY(), old.getBlockZ() + 0.5, old.getYaw(), old.getPitch());
                            m.registerSpawnpoint(index, spawn);
                            context.reply("&aSpawnpoint for index &e" + index + String.format(" &ahas been set to &e%s&a, &e%s&a, &e%s&a.", spawn.getX(), spawn.getY(), spawn.getZ()));
                        } else {
                            TeamColor color = context.resolve(2, TeamColor.class);
                            if (!arena.getTeams().contains(color)) {
                                MessageKey.TEAM_NOT_REGISTERED.send(context.player(), arena, color, null, null, context.getCommand().getName(),
                                        null, -1, ex);
                                return;
                            }
                            Location old = context.player().getLocation();
                            Location spawn = new Location(old.getWorld(), old.getBlockX() + 0.5, old.getBlockY(), old.getBlockZ() + 0.5, old.getYaw(), old.getPitch());
                            arena.registerSpawnPoint(color, spawn);
                            MessageKey.SPAWNPOINT_SET.send(context.player(), arena, color, spawn, null, context.getCommand().getName(),
                                    null, -1, ex);
                        }
                        return;
                    }
                    case "lobby":
                        if (arena.getArenaType() == ArenaType.FREE_FOR_ALL) {
                            FFAManager m = arena.getFFAManager();
                            int index = context.resolve(2, int.class);
                            if (index > arena.getMaxPlayerCount()) {
                                context.reply(String.format("&cValue &e%s &cis greater than the arena's maximum count (&e%s&c)", index, arena.getMaxPlayerCount()));
                                return;
                            }
                            Location old = context.player().getLocation();
                            Location spawn = new Location(old.getWorld(), old.getBlockX() + 0.5, old.getBlockY(), old.getBlockZ() + 0.5, old.getYaw(), old.getPitch());
                            m.registerLobby(index, spawn);
                            context.reply("&aLobby for index &e" + index + String.format(" &ahas been set to &e%s&a, &e%s&a, &e%s&a.", spawn.getX(), spawn.getY(), spawn.getZ()));
                        } else {
                            TeamColor color = context.resolve(2, TeamColor.class);
                            if (!arena.getTeams().contains(color)) {
                                MessageKey.TEAM_NOT_REGISTERED.send(context.player(), arena, color, null, null, context.getCommand().getName(),
                                        null, -1, ex);
                                throw new CommandCallbackException();
                            }
                            Location old = context.player().getLocation();
                            Location lobby = new Location(old.getWorld(), old.getBlockX() + 0.5, old.getBlockY(), old.getBlockZ() + 0.5, old.getYaw(), old.getPitch());
                            arena.getTeamLobbies().put(color, lobby);
                            context.reply("&aLobby for team &e" + color.chat() + String.format(" &ahas been set to &e%.1f&a, &e%.1f&a, &e%.1f&a.", lobby.getX(), lobby.getY(), lobby.getZ()));
                        }
                        return;
                    case "removelobby": {
                        if (arena.getArenaType() == ArenaType.FREE_FOR_ALL) {
                            FFAManager m = arena.getFFAManager();
                            int index = context.resolve(2, int.class);
                            if (index > arena.getMaxPlayerCount())
                                throw new CommandCallbackException(String.format("&cValue &e%s &cis greater than the arena's maximum count (&e%s&c)", index, arena.getMaxPlayerCount()));
                            m.removeLobby(index);
                            context.reply("&aLobby for index &e" + index + "has been removed.");
                        } else {
                            TeamColor color = context.resolve(2, TeamColor.class);
                            if (!arena.getTeams().contains(color)) {
                                MessageKey.TEAM_NOT_REGISTERED.send(context.player(), arena, color, null, null, context.getCommand().getName(),
                                        null, -1, ex);
                                throw new CommandCallbackException();
                            }
                            arena.getTeamLobbies().remove(color);
                            context.reply("&aLobby for team &e" + color.chat() + " &ahas been removed.");
                        }
                        return;
                    }
                }
                return;
            case 4:
                switch (args[0].toLowerCase()) {
                    case "create": {
                        context.requirePlayer();
                        GameArena found = GameArena.getByKey(args[1]);
                        if (found != null) { // An arena with that key already exists
                            MessageKey.ARENA_ALREADY_EXISTS.send(context.player(), found, null, null, null, context.getCommand().getName(),
                                    null, -1, ex);
                            throw new CommandCallbackException();
                        }
                        if (!FileManager.isValidPath(args[1]))
                            throw new CommandCallbackException("&cInvalid arena name: &e" + args[1] + "&c.");
                        R newArena = arenaFactory.create(args[1], context.join(3), CopyStore.LOCATIONS.get(context.player()), context.resolve(2, ArenaType.class), ex);
                        if (newArena.getArenaType() == ArenaType.FREE_FOR_ALL) newArena.setMaxPlayerCount(2);
                        SpleefX.getPlugin().getArenaManager().add(context.player(), newArena, context.getCommand().getName());
                        return;
                    }
                    case "settings": {
                        switch (args[2].toLowerCase()) {
                            case "displayname":
                                arena.setDisplayName(context.join(3));
                                context.reply("&aArena &e" + arena.getKey() + "&a's display name has been set to &d" + arena.getDisplayName());
                                return;
                            case "teams":
                                if (arena.getArenaType() == ArenaType.FREE_FOR_ALL) {
                                    context.reply("&cYou cannot add teams to FFA arenas!");
                                    return;
                                }
                                List<TeamColor> add = Arrays.stream(Arrays.copyOfRange(args, 3, args.length)).map(color -> context.resolve(color, TeamColor.class)).collect(Collectors.toList());
                                add.removeIf(arena.getTeams()::contains);
                                arena.getTeams().addAll(add);
                                add.forEach(team -> arena.gameTeams.add(new GameTeam(team, new ArrayList<>())));
                                context.reply("&aSuccessfully added teams: " + CommandContext.joinNiceString(add.stream().map(TeamColor::chat).toArray(String[]::new)));
                                return;
                            case "membersperteam": {
                                arena.setMembersPerTeam(context.resolve(3, int.class));
                                context.reply("&aArena &e" + arena.getKey() + "&a's members per team count has been set to &e" + arena.getMembersPerTeam());
                                return;
                            }
                            case "gametime": {
                                arena.setGameTime(context.resolve(3, int.class));
                                context.reply("&aArena &e" + arena.getKey() + "&a's game time has been set to &e" + arena.getGameTeams());
                                return;
                            }
                            case "deathlevel": {
                                arena.setDeathLevel(context.resolve(3, int.class));
                                context.reply("&aArena &e" + arena.getKey() + "&a's death level has been set to &e" + arena.getDeathLevel());
                                return;
                            }
                            case "bet": {
                                arena.setBet(context.resolve(3, int.class));
                                context.reply("&aArena &e" + arena.getKey() + "&a's betting has been set to &e" + arena.getBet());
                                return;
                            }
                            case "minimum": {
                                arena.setMinimum(context.resolve(3, int.class));
                                context.reply("&aArena &e" + arena.getKey() + "&a's minimum players required has been set to &e" + arena.getMinimum());
                                return;
                            }
                            case "maxplayercount": {
                                if (arena.getArenaType() == ArenaType.TEAMS)
                                    throw new CommandCallbackException("&cYou cannot set the maximum player count in teams! It is the multiplication of members per team by the teams count.");
                                arena.setMaxPlayerCount(context.resolve(3, int.class));
                                context.reply("&aArena &e" + arena.getKey() + "&a's max player count has been set to &e" + arena.getMaxPlayerCount());
                                return;
                            }
                        }
                    }
                }
                return;
        }
        //context.invalidUsage();
    }

    /**
     * A simple interface for creating arena instances
     */
    @FunctionalInterface
    public interface ArenaFactory<R extends GameArena> {

        /**
         * Creates a new arena from the specified data
         *
         * @param key               Arena key
         * @param displayName       Arena display name
         * @param regenerationPoint Regeneration point for the arena
         * @param arenaType         The arena's type
         * @param extension         Arena's extension mode. Can be null.
         * @return The arena
         */
        R create(String key, String displayName, Location regenerationPoint, ArenaType arenaType, GameExtension extension);

    }
}