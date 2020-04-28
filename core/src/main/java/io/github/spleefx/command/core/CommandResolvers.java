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
package io.github.spleefx.command.core;

import io.github.spleefx.arena.api.ArenaType;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.command.core.CommandCallback.CommandCallbackException;
import io.github.spleefx.command.core.PluginSubcommand.ParameterResolver;
import io.github.spleefx.command.core.PluginSubcommand.TabContext;
import io.github.spleefx.command.core.PluginSubcommand.TabProvider;
import io.github.spleefx.economy.booster.BoosterFactory;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.team.TeamColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A class for resolving specific parameters
 */
public class CommandResolvers {

    /**
     * An instance of all the resolvers
     */
    public static final CommandResolvers RESOLVERS = new CommandResolvers();

    /**
     * A map of all resolvers
     */
    private Map<Class<?>, Resolver<?>> resolvers = new HashMap<>();

    /**
     * A map of all tab providers
     */
    private Map<String, TabProvider> tabProviders = new HashMap<>();

    /**
     * A map of all tab providers
     */
    private Map<String, List<String>> staticTabs = new HashMap<>();

    /**
     * Creates an instance of all the resolvers
     */
    public CommandResolvers() {
        // register primitives
        registerResolver(int.class, new Resolver<>("number", (a, c) -> Integer.parseInt(a)));
        registerResolver(float.class, new Resolver<>("number", (a, c) -> Float.parseFloat(a)));
        registerResolver(double.class, new Resolver<>("number", (a, c) -> Double.parseDouble(a)));

        // register plugin types
        registerResolver(GameArena.class, new Resolver<>("arena", (a, c) -> GameArena.getByKey(a)).setFail(
                (a, c) -> {
                    MessageKey.INVALID_ARENA.send(c.getSender(), null, null, null, null, null, null, -1, c.getExtension(), "{arena}", a);
                    throw new CommandCallbackException();
                }
        ));
        registerResolver(TeamColor.class, new Resolver<>("team", (a, c) -> TeamColor.getNullable(a)));
        registerResolver(ArenaType.class, new Resolver<>("arena type", (a, c) -> ArenaType.lookup(a))
                .setFail((a, c) -> {
                    throw new CommandCallbackException("&cInvalid arena type: &e" + a + "&c. Available types: &bffa&c, &bteams&c.");
                }));
        registerResolver(GameExtension.class, new Resolver<>("extension", (a, c) -> {
            GameExtension ex = ExtensionsManager.getByKey(a);
            return ex == null ? ExtensionsManager.getFromCommand(c.getCommand().getName()) : ex;
        }));
        registerResolver(BoosterFactory.class, new Resolver<>("booster type", (a, c) -> BoosterFactory.get(a)));

        // register Bukkit types
        registerResolver(Player.class, new Resolver<>("player", (a, c) -> Bukkit.getPlayer(a)));
        registerResolver(OfflinePlayer.class, new Resolver<>("player", (a, c) -> Bukkit.getOfflinePlayer(a)));

        registerTabProvider("_extensions", context -> {
            switch (context.getArgs()[0]) {
                case "disable":
                    return ExtensionsManager.EXTENSIONS.values().stream().filter(GameExtension::isEnabled).map(GameExtension::getKey).filter(c -> c.startsWith(context.getArgs()[1])).collect(Collectors.toList());
                case "enable":
                    return ExtensionsManager.EXTENSIONS.values().stream().filter(e -> !e.isEnabled()).map(GameExtension::getKey).filter(c -> c.startsWith(context.getArgs()[1])).collect(Collectors.toList());
                case "reload":
                    return ExtensionsManager.EXTENSIONS.keySet().stream().filter(c -> c.startsWith(context.getArgs()[1])).collect(Collectors.toList());
                default:
                    return Collections.emptyList();
            }
        });
        registerTabProvider("_etypes", context -> {
            String[] args = context.getArgs();
            return args.length == 3 && args[0].equalsIgnoreCase("reload") ? StaticTabs.EXTENSION_TYPES : Collections.emptyList();
        });
        registerTabProvider("numbers", context -> context.getArgs()[0].equalsIgnoreCase("reset") ? Collections.emptyList() : StaticTabs.NUMBERS);
        registerTabProvider("rlConfirm", context -> context.getArgs()[0].contains("arenas") ? StaticTabs.CONFIRM : Collections.emptyList());
        registerTabProvider("ext_arenas", context -> context.getArgs()[0].equalsIgnoreCase("create") ? Collections.emptyList() : GameArena.ARENAS.get().values().stream().filter(e -> e.getExtension().equals(context.getFakeContext().getExtension())).map(GameArena::getKey).collect(Collectors.toList()));
        registerTabProvider("arenacompl", context -> {
            String[] args = context.getArgs();
            if (args.length == 3) {
                GameArena arena = GameArena.getByKey(args[1]);
                if (arena == null && !args[0].equalsIgnoreCase("create"))
                    return Collections.emptyList();
                switch (args[0].toLowerCase()) {
                    case "spawnpoint":
                    case "lobby":
                    case "removelobby":
                        if (arena.getArenaType() == ArenaType.TEAMS)
                            return arena.getTeams().stream().map(team -> team.getName().toLowerCase()).collect(Collectors.toList());
                        return IntStream.rangeClosed(1, arena.getMaxPlayerCount()).mapToObj(Integer::toString).collect(Collectors.toCollection(LinkedList::new));
                    case "create":
                        return StaticTabs.TYPES;
                    case "settings":
                        return StaticTabs.SETTINGS;
                }
            }
            if (args[0].toLowerCase().equals("settings")) {
                GameArena gameArena = GameArena.getByKey(args[1]);
                if (args[2].equalsIgnoreCase("teams")) {
                    if (gameArena.getArenaType() == ArenaType.FREE_FOR_ALL) return Collections.emptyList();
                    List<String> teams = new ArrayList<>(StaticTabs.TEAMS);
                    teams.removeIf(t -> gameArena.getTeams().contains(TeamColor.get(t)));
                    return teams.stream().filter(s -> s.startsWith(args[args.length - 1])).collect(Collectors.toList());
                }
                return Collections.emptyList();
            }
            return Collections.emptyList();
        });
        registerTabProvider("commands", context -> context.getHandler().getCommands().values().stream().filter(c -> context.getSender().hasPermission(c.generatePermission(context.getFakeContext().getExtension()))).map(c -> c.name).collect(Collectors.toList()));
        registerTabProvider("stats", context -> {
            String[] args = context.getArgs();
            return args.length == 2 ? StaticTabs.STATS : args.length == 1 ? (!args[0].isEmpty() && "global".startsWith(args[0]) ? StaticTabs.STATS : null) : Collections.emptyList();
        });
        registerTabProvider("arenas", context -> GameArena.ARENAS.get().values().stream().filter(a -> a.getExtension().equals(context.getFakeContext().getExtension())).map(GameArena::getKey).collect(Collectors.toList()));
        registerStaticTab("players", StaticTabs.NULL); // bukkit handles that by itself.
    }

    public Resolver<?> get(Class<?> clazz) {
        return resolvers.get(clazz);
    }

    public List<String> getTab(String key, TabContext context) {
        List<String> tabs = staticTabs.get(key);
        if (tabs != null && tabs.equals(StaticTabs.NULL)) return null;
        if (tabs == null) {
            TabProvider provider = tabProviders.get(key);
            if (provider == null)
                throw new IllegalArgumentException("Cannot find tabs for key " + key);
            tabs = provider.getTab(context);
        }
        return tabs;
    }

    private void registerStaticTab(String key, List<String> tabs) {
        staticTabs.put(key, tabs);
    }

    private void registerTabProvider(String key, TabProvider provider) {
        tabProviders.put(key, provider);
    }

    /**
     * Registers the specified resolver. This is mainly wrapping the map's actions so that we can run
     * compiler checks to ensure correct types are passed to the map
     *
     * @param resolvedType Type to resolve
     * @param resolver     The resolver
     * @param <R>          Type to be resolved
     */
    private <R> void registerResolver(Class<R> resolvedType, Resolver<R> resolver) {
        resolvers.put(resolvedType, resolver);
    }

    /**
     * A resolver wrapper for handling exceptions
     *
     * @param <R> Type to resolve into
     */
    public static class Resolver<R> {

        /**
         * The internal resolver
         */
        private ParameterResolver<R> resolver;

        /**
         * The name in which "invalids" will appear with
         */
        private String name;

        /**
         * A consumer to run if the command fails. Can be left to use the standard fallback
         */
        private BiConsumer<String, CommandContext> onFail;

        /**
         * Creates a new resolver wrapper
         *
         * @param name     Name of the resolver's "invalid"
         * @param resolver The internal resolver
         */
        public Resolver(String name, ParameterResolver<R> resolver) {
            this.resolver = resolver;
            this.name = name;
        }

        /**
         * Sets the fail fallback consumer
         *
         * @param onFail Task to run when failed
         * @return This resolver instance
         */
        public Resolver<R> setFail(BiConsumer<String, CommandContext> onFail) {
            this.onFail = onFail;
            return this;
        }

        /**
         * Resolves the specified type
         *
         * @param argument Argument to resolve from
         * @param context  Command context
         * @return The resolved type
         * @throws CommandCallbackException If the type could not be resolved
         */
        public R resolve(String argument, CommandContext context) {
            try {
                R resolved = resolver.resolve(argument, context);
                if (resolved == null)
                    throw new NullPointerException(); // this will redirect us down below
                return resolved;
            } catch (Exception e) {
                if (onFail == null && !(e instanceof CommandCallbackException))
                    throw new CommandCallbackException("&cInvalid " + name + ": &e" + argument + "&c.");
                if (onFail != null)
                    onFail.accept(argument, context);
                return null;
            }
        }
    }

    interface StaticTabs {

        List<String> CONFIRM = Collections.singletonList("confirm");
        List<String> STATS = Collections.singletonList("global");
        List<String> EXTENSION_TYPES = Arrays.asList("standard", "custom");
        List<String> NUMBERS = IntStream.range(0, 1001).filter(i -> i % 100 == 0).mapToObj(Integer::toString).collect(Collectors.toList());
        List<String> NULL = Collections.singletonList("null");
        List<String> TYPES = Arrays.asList("ffa", "teams");
        List<String> SETTINGS = Arrays.asList("bet", "deathLevel", "disable", "displayName", "enable", "gameTime", "maxPlayerCount", "membersPerTeam", "minimum", "teams", "toggle");
        List<String> TEAMS = Arrays.stream(TeamColor.values()).filter(TeamColor::isUsable).map(c -> c.name().toLowerCase()).collect(Collectors.toList());

    }
}
