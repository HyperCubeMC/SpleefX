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
package io.github.spleefx.arena.api;

import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.ArenaPlayer.ArenaPlayerState;
import io.github.spleefx.arena.ArenaStage;
import io.github.spleefx.arena.api.GameTask.Phase;
import io.github.spleefx.config.SpleefXConfig;
import io.github.spleefx.data.GameStatType;
import io.github.spleefx.data.PlayerProfile;
import io.github.spleefx.data.PlayerRepository;
import io.github.spleefx.data.TempStatsTracker;
import io.github.spleefx.extension.ExtensionTitle;
import io.github.spleefx.extension.GameEvent;
import io.github.spleefx.extension.GameExtension.ScoreboardType;
import io.github.spleefx.extension.GameExtension.SenderType;
import io.github.spleefx.extension.ability.DoubleJumpHandler.DataHolder;
import io.github.spleefx.extension.ability.DoubleJumpHandler.DoubleJumpItems;
import io.github.spleefx.extension.ability.GameAbility;
import io.github.spleefx.perk.GamePerk;
import io.github.spleefx.scoreboard.ScoreboardHolder;
import io.github.spleefx.sign.SignManager;
import io.github.spleefx.spectate.PlayerSpectateAnotherEvent;
import io.github.spleefx.spectate.SpectatePlayerMenu;
import io.github.spleefx.team.GameTeam;
import io.github.spleefx.team.TeamColor;
import io.github.spleefx.util.PlaceholderUtil.BetEntry;
import io.github.spleefx.util.PlaceholderUtil.ColoredNumberEntry;
import io.github.spleefx.util.code.MapBuilder;
import io.github.spleefx.util.game.BukkitTaskUtils;
import io.github.spleefx.util.game.Metas;
import io.github.spleefx.util.game.PlayerContext;
import io.github.spleefx.util.message.message.Message;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.github.spleefx.SpleefX.getPlugin;
import static io.github.spleefx.SpleefX.getSpectatorSettings;
import static io.github.spleefx.config.SpleefXConfig.*;
import static io.github.spleefx.util.PlaceholderUtil.NUMBER_FORMAT;

public abstract class BaseArenaEngine<R extends GameArena> implements ArenaEngine {

    public static final EnumMap<GameAbility, Integer> DEFAULT_MAP = new EnumMap<>(GameAbility.class);

    protected Map<UUID, EnumMap<GameAbility, Integer>> abilityCount = new HashMap<>();

    protected Map<UUID, Map<GamePerk, Integer>> perksCount = new HashMap<>();

    /**
     * Represents all tasks that are ran when the game is over
     */
    private final List<GameTask> endTasks = new ArrayList<>();

    /**
     * A predicate for validating whether an arena is fully setup or not.
     * <p>
     * If {@link Predicate#test(Object)} returns true this means that the arena requires setup
     */
    private static final Predicate<GameArena> SETUP_VALIDATION = arena -> {
        if (arena.getArenaType() != ArenaType.FREE_FOR_ALL)
            return arena.getTeams().size() < 2 || !arena.getSpawnPoints().keySet().containsAll(arena.getTeams());
        else
            return !FFAManager.IS_READY.test(arena);
    };

    private final Map<UUID, TempStatsTracker> statsTrackerMap = new HashMap<>();

    private final List<ArenaPlayer> spectators = new ArrayList<>();

    public final Map<UUID, ItemStack> playerHeads = new ConcurrentHashMap<>();

    private final List<ArenaPlayer> dead = new ArrayList<>();
    private final List<GameTeam> deadTeams = new ArrayList<>();
    private final Set<UUID> broadcasted = new HashSet<>();
    private final Map<Player, Integer> betsMap = new HashMap<>();

    /**
     * The arena that is subject to processing
     */
    protected R arena;

    /**
     * A list of all players in the arena
     */
    private final Map<ArenaPlayer, GameTeam> playerTeams = new HashMap<>();

    /**
     * The currently displayed scoreboard
     */
    private ScoreboardType currentScoreboard = ScoreboardType.WAITING_IN_LOBBY;

    /**
     * Represents all alive players
     */
    private final List<Player> alive = new CopyOnWriteArrayList<>();

    /**
     * Time left to start
     */
    public int countdown = SpleefXConfig.COUNTDOWN_ON_ENOUGH_PLAYERS.get();

    /**
     * The task that controls the cooldown
     */
    private BukkitTask countdownTask;

    /**
     * The task that controls the game
     */
    private BukkitTask gameTask;

    /**
     * The task that controls the game time
     */
    private BukkitTask timerTask;

    /**
     * The time left till the game ends
     */
    public int timeLeft;
    private int origTimeLeft;

    /**
     * The arena sign manager
     */
    private final SignManager signManager;

    /**
     * Creates an engine for the specified arena
     *
     * @param arena Arena to create for
     */
    public BaseArenaEngine(R arena) {
        this.arena = arena;
        signManager = new SignManager(arena);
        timeLeft = arena.getGameTime() * 60;
    }

    /**
     * Returns the current game stage
     *
     * @return The current game stage
     */
    @Override
    public ArenaStage getArenaStage() {
        if (arena == null) return ArenaStage.DISABLED;
        if (!arena.isEnabled() || !arena.getExtension().isEnabled()) {
            return arena.stage = ArenaStage.DISABLED;
        }
        if (SETUP_VALIDATION.test(arena))
            return arena.stage = ArenaStage.NEEDS_SETUP;
        if (arena.stage == null || arena.stage == ArenaStage.NEEDS_SETUP || (arena.stage == ArenaStage.DISABLED && arena.isEnabled() && arena.getExtension().isEnabled()))
            return arena.stage = ArenaStage.WAITING;
        return arena.stage;
    }

    /**
     * Updates the game stage and all the subordinate signs
     *
     * @param stage New stage to set
     */
    @Override
    public void setArenaStage(ArenaStage stage) {
        arena.stage = stage;
        getSignManager().update();
    }

    /**
     * Selects a team randomly as long as it's not full
     *
     * @return The randomly selected team
     */
    @Override
    public GameTeam selectTeam() {
        if (arena.getArenaType() == ArenaType.FREE_FOR_ALL) return arena.getGameTeams().get(0); // The "FFA" team
        GameTeam team = arena.getGameTeams().get(ThreadLocalRandom.current().nextInt(arena.getGameTeams().size()));
        if (team.getMembers().size() == arena.getMembersPerTeam()) return selectTeam();
        return team;
    }

    /**
     * Joins the player to the arena
     *
     * @param p Player to join
     */
    @Override
    public synchronized boolean join(ArenaPlayer p, @Nullable GameTeam team) {
        Player player = p.getPlayer();
        if (p.getCurrentArena() != null) {
            Message.ALREADY_IN_ARENA.reply(player, arena, player, -1, arena.getExtension());
            return false;
        }

        if (!arena.isEnabled() || !arena.getExtension().isEnabled()) {
            Message.ARENA_DISABLED.reply(player, arena, player, arena.getExtension());
            return false;
        }
        if (!isEmpty(player.getInventory()) && (boolean) ARENA_REQUIRE_EMPTY_INV.get()) {
            Message.MUST_HAVE_EMPTY_INV.reply(player, arena, player, arena.getExtension());
            return false;
        }
        if (isFull()) {
            Message.ARENA_FULL.reply(player, arena, player, arena.getExtension());
            return false;
        }
        PlayerProfile stats = null;
        if (arena.shouldTakeBets()) {
            if ((Objects.requireNonNull(stats = PlayerRepository.REPOSITORY.lookup(player))).getCoins() < arena.getBet()) {
                Message.NOT_ENOUGH_TO_BET.reply(player, arena, player, arena.getExtension(), new BetEntry(arena.getBet(), null));
                return false;
            }
        }
        switch (arena.getEngine().getArenaStage()) {
            case DISABLED:
                Message.ARENA_DISABLED.reply(player, arena, player, arena.getExtension());
                return false;
            case ACTIVE:
                Message.ARENA_ALREADY_ACTIVE.reply(player, arena, player, arena.getExtension());
                return false;
            case REGENERATING:
                Message.ARENA_REGENERATING.reply(player, arena, player, arena.getExtension());
                return false;
            case NEEDS_SETUP:
                Message.ARENA_NEEDS_SETUP.reply(player, arena, player, arena.getExtension());
                return false;
        }
        if (playerTeams.containsKey(p)) return false;
        if (team == null)
            team = selectTeam();
        team.getMembers().add(player);
        playerTeams.put(p, team);
        prepare(p, team);
        broadcasted.add(player.getUniqueId());
        if (arena.getArenaType() == ArenaType.TEAMS) {
            for (Player pl : toBroadcast()) {
                Message.PLAYER_JOINED_T.reply(pl, arena, team.getColor(), player, arena.getExtension());
            }
        } else {
            for (Player pl : toBroadcast()) {
                Message.PLAYER_JOINED_FFA.reply(pl, arena, team.getColor(), player, arena.getExtension());
            }
        }
        getSignManager().update();
        if (playerTeams.size() >= arena.getMinimum())
            countdown();
        abilityCount.put(player.getUniqueId(), (EnumMap<GameAbility, Integer>)
                MapBuilder.of(new EnumMap<GameAbility, Integer>(GameAbility.class))
                        .put(GameAbility.DOUBLE_JUMP, arena.getExtension().getDoubleJumpSettings().getDefaultAmount()).build());
        if (arena.shouldTakeBets()) {
            betsMap.put(player, arena.getBet());
            assert stats != null;
            getTracker(player).subtractCoins(arena.getBet());
//            stats.takeCoins(player, arena.getBet());
            Message.BET_TAKEN.reply(player, arena, player, arena.getExtension(), new BetEntry(arena.getBet(), null));
        }
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    private static boolean isEmpty(PlayerInventory inventory) {
        return Arrays
                .stream(inventory.getContents())
                .noneMatch(itemStack -> itemStack != null && !itemStack.getType().name().contains("AIR")) &&
                Arrays
                        .stream(inventory.getArmorContents())
                        .noneMatch(itemStack -> itemStack != null && !itemStack.getType().name().contains("AIR"));
    }

    /**
     * Invoked when the player quits
     *
     * @param p Player to quit
     */
    @Override
    public synchronized void quit(ArenaPlayer p, boolean disconncet) {
        Player player = p.getPlayer();
        GameTeam team = playerTeams.remove(p);
        if (team != null) {
            team.getAlive().remove(player);
            team.getMembers().remove(player);
        }
        alive.remove(player);

        playerHeads.remove(player.getUniqueId());
        if (getArenaStage() == ArenaStage.ACTIVE && !disconncet) {
            getPlugin().getSpectatingHandler().disableSpectationMode(p.getPlayer());
            spectators.remove(p);
        } else if (getArenaStage() == ArenaStage.COUNTDOWN || getArenaStage() == ArenaStage.WAITING) {
            statsTrackerMap.remove(p.getPlayer().getUniqueId());
            broadcasted.remove(p.getPlayer().getUniqueId());
            if (arena.shouldTakeBets()) {
                PlayerRepository.REPOSITORY.apply(p.getPlayer().getUniqueId(), (playerProfile, builder) -> builder.addCoins(betsMap.remove(player)));
            }
        }
        load(p, false);
        if (getArenaStage() != ArenaStage.ACTIVE) {
            if (playerTeams.size() < arena.getMinimum() && !BukkitTaskUtils.isCancelled(countdownTask)) {
                countdownTask.cancel();
                countdown = SpleefXConfig.COUNTDOWN_ON_ENOUGH_PLAYERS.get();
                setArenaStage(ArenaStage.WAITING);
                currentScoreboard = ScoreboardType.WAITING_IN_LOBBY;
                broadcast(Message.NOT_ENOUGH_PLAYERS);
                toBroadcast().forEach(ap -> {
                    ap.setLevel(0);
                    ap.setExp(0);
                });
            }
        }
    }

    /**
     * Invoked when the player loses
     *
     * @param player Player to lose
     * @param team   The player's team
     */
    @Override
    public synchronized void lose(ArenaPlayer player, GameTeam team, boolean disconnect) {
        Player p = player.getPlayer();
        playerHeads.remove(player.getPlayer().getUniqueId());
        dead.add(ArenaPlayer.adapt(p));

        getTracker(p).replaceExtensionStat(arena.getExtension().getKey(), GameStatType.LOSSES, i -> i + 1);
//        getPlugin().getDataProvider().add(PlayerStatistic.LOSSES, p, arena.getExtension(), 1);

        if (getArenaStage() == ArenaStage.ACTIVE && !disconnect) {
            if (alive.size() > 2) {
                getPlugin().getSpectatingHandler().putSpectationMode(p);
                spectators.add(player);
                Player target = alive.stream().filter(pl -> !pl.getUniqueId().equals(player.getPlayer().getUniqueId()))
                        .findAny().orElse(null);
                if (target != null) {
                    player.getPlayer().setGameMode(GameMode.SPECTATOR);
                    player.getPlayer().setSpectatorTarget(target);
                    player.getPlayer().setExp(0);
                    player.getPlayer().setLevel(0);
                    Bukkit.getPluginManager().callEvent(new PlayerSpectateAnotherEvent(player.getPlayer(), target, arena));
                }
            }
        }
        if (disconnect || alive.size() <= 2) {
            quit(player, true);
        }
        if (team != null && team.getAlive() != null)
            team.getAlive().remove(player.getPlayer());
        alive.remove(player.getPlayer());
        Objects.requireNonNull(team);
        if (arena.getArenaType() == ArenaType.TEAMS)
            toBroadcast().forEach((pz) -> Message.PLAYER_LOST_T.reply(pz.getPlayer(), arena, team.getColor(), player.getPlayer(), -1, arena.getExtension()));
        else
            toBroadcast().forEach((pz) -> Message.PLAYER_LOST_FFA.reply(pz.getPlayer(), arena, team.getColor(), player.getPlayer(), -1, arena.getExtension()));
        arena.getExtension().getGameTitles().get(GameEvent.LOSE).display(player.getPlayer());
        playerTeams.remove(player);
    }

    /**
     * Invoked when the player wins
     *
     * @param p    Player to win
     * @param team The player's team
     */
    @Override
    public void win(ArenaPlayer p, GameTeam team) {
        Player player = p.getPlayer();

        getTracker(player).replaceExtensionStat(arena.getExtension().getKey(), GameStatType.WINS, i -> i + 1);
        //getPlugin().getDataProvider().add(PlayerStatistic.WINS, player, arena.getExtension(), 1);
        List<Player> all = broadcasted.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).collect(Collectors.toList());
        if (arena.getArenaType() == ArenaType.FREE_FOR_ALL) {
            for (Player e : all) {
                arena.getExtension().getGameTitles().get(GameEvent.WIN).display(e.getPlayer(), player.getName());
                Message.PLAYER_WINS_FFA.reply(e, arena, team.getColor(), p.getPlayer(), -1, arena.getExtension());
            }
            dead.add(p);
        } else {
            for (Player e : all) {
                arena.getExtension().getGameTitles().get(GameEvent.WIN).display(e.getPlayer(), team.getColor().chat());
                Message.PLAYER_WINS_T.reply(e, arena, team.getColor(), p.getPlayer(), -1, arena.getExtension());
            }
            deadTeams.add(team);
        }
        load(p, true);
    }

    /**
     * Invoked when the game result is draw
     */
    @Override
    public void draw() {
        gameTask.cancel();
        timerTask.cancel();
        timeLeft = arena.getGameTime() * 60;
        toBroadcast().forEach(p -> {
            load(ArenaPlayer.adapt(p), true);
            arena.getExtension().getGameTitles().get(GameEvent.DRAW).display(p.getPlayer());

            getTracker(p).replaceExtensionStat(arena.getExtension().getKey(), GameStatType.DRAWS, i -> i + 1);
            //getPlugin().getDataProvider().add(PlayerStatistic.DRAWS, p.getPlayer(), arena.getExtension(), 1);
        });
        end(false);
    }

    /**
     * Prepares the player to the game, by adding effects
     *
     * @param p    Player to prepare
     * @param team The player's team
     */
    @Override
    public void prepare(ArenaPlayer p, GameTeam team) {
        save(p);
        Player player = p.getPlayer();
        ArenaPlayer.adapt(player).setState(ArenaPlayerState.WAITING).setCurrentArena(arena);
        player.setGameMode(arena.getExtension().getWaitingMode());
        if (team.getColor() != TeamColor.FFA) {
            Location lobby = arena.getTeamLobbies().getOrDefault(team.getColor(), arena.getLobby());
            player.teleport(lobby == null ? arena.getSpawnPoints().get(team.getColor()) : lobby);
        } else if (arena.getLobby() == null) {
            Location lobby = arena.getFFAManager().getLobby(player, arena);
            player.teleport(lobby == null ? arena.getFFAManager().getSpawnpoint(arena, player) : lobby);
        } else {
            player.teleport(arena.getLobby());
            arena.getFFAManager().getSpawnpoint(arena, player);
        }
        if (arena.getExtension().getQuitItem().give)
            player.getInventory().setItem(arena.getExtension().getQuitItem().slot, arena.getExtension().getQuitItem().factory().create());
        displayScoreboard(p);
    }

    /**
     * Prepares the player to the game, by teleporting, adding items, etc
     *
     * @param arenaPlayer Player to prepare
     * @param team        The player's team
     */
    @Override
    public void prepareForGame(ArenaPlayer arenaPlayer, GameTeam team) {
        Player player = arenaPlayer.getPlayer();
        team.getAlive().add(player);

        arenaPlayer.setState(ArenaPlayerState.IN_GAME)
                .setCurrentArena(arena);

        player.setGameMode(arena.getExtension().getIngameMode());
        arena.getExtension().getGivePotionEffects().forEach(player::addPotionEffect);
        if (arena.getArenaType() == ArenaType.FREE_FOR_ALL)
            player.teleport(arena.getFFAManager().get(arena.getFFAManager().getIndex(player)));
        else
            player.teleport(arena.getSpawnPoints().get(team.getColor()));
        player.getInventory().clear();
        arena.getExtension().getItemsToAdd().forEach((slot, item) -> player.getInventory().setItem(slot, item.factory().create()));
        arena.getExtension().getArmorToAdd().forEach((slot, item) -> slot.set(player, item.factory().create()));

        //getPlugin().getDataProvider().add(PlayerStatistic.GAMES_PLAYED, player, arena.getExtension(), 1);
        getTracker(player).replaceExtensionStat(arena.getExtension().getKey(), GameStatType.GAMES_PLAYED, i -> i + 1);

        DataHolder doubleJumpSettings = arena.getExtension().getDoubleJumpSettings();
        if (!doubleJumpSettings.isEnabled()) return;
        if (doubleJumpSettings.getDefaultAmount() > 0) {
            player.setAllowFlight(true);
            addDoubleJumpItems(arenaPlayer, true);
        }
        arenaPlayer.getStats().getPerks().forEach((perk, count) -> {
            if (perk.consumeFrom(arenaPlayer)) {
                perksCount.computeIfAbsent(player.getUniqueId(), (k) -> new HashMap<>()).put(perk, perk.getPurchaseSettings().getIngameAmount());
                if (perk.canUse(arena.getExtension()))
                    perk.giveToPlayer(arenaPlayer);
            }
        });
    }

    public void addDoubleJumpItems(ArenaPlayer player, boolean newState) {
        DataHolder doubleJumpSettings = arena.getExtension().getDoubleJumpSettings();
        if (!doubleJumpSettings.isEnabled() || player.isSpectating()) return;
        DoubleJumpItems items = doubleJumpSettings.getDoubleJumpItems();
        if (items.isEnabled() && doubleJumpSettings.getDefaultAmount() > 0)
            player.getPlayer().getInventory().setItem(items.getSlot(), newState ? items.getAvailable().factory().create() : items.getUnavailable().factory().create());
    }

    /**
     * Runs the countdown
     */
    @SneakyThrows @Override
    public void countdown() {
        if (countdownTask != null && !BukkitTaskUtils.isCancelled(countdownTask)) return;
        currentScoreboard = isFull() ? ScoreboardType.COUNTDOWN_AND_FULL : ScoreboardType.COUNTDOWN_AND_WAITING;
        setArenaStage(ArenaStage.COUNTDOWN);
        if (ARENA_REGENERATE_BEFORE_COUNTDOWN.get())
            getPlugin().getArenaManager().regenerateArena(arena.getKey());

        Map<Integer, String> numbersToDisplay = TITLE_ON_COUNTDOWN_NUMBERS.get();
        playerTeams.forEach((p, team) -> Message.GAME_STARTING.reply(p.getPlayer(), arena, team.getColor(), p.getPlayer(), new ColoredNumberEntry(numbersToDisplay.getOrDefault(countdown + "", "&e" + countdown)), countdown, arena.getExtension()));
        arena.getExtension().getRunCommandsWhenGameFills().forEach(c -> SenderType.CONSOLE.run(null, c, arena));
        countdownTask = Bukkit.getScheduler().runTaskTimer(getPlugin(), () -> {
            countdown--;
            currentScoreboard = isFull() ? ScoreboardType.COUNTDOWN_AND_FULL : ScoreboardType.COUNTDOWN_AND_WAITING;
            playerTeams.forEach((p, value) -> {
                if (DISPLAY_COUNTDOWN_ON_EXP_BAR.get()) {
                    p.getPlayer().setLevel(countdown);
                    p.getPlayer().setExp((float) countdown / ((Number) COUNTDOWN_ON_ENOUGH_PLAYERS.get()).intValue());
                }
                String title = numbersToDisplay.get(countdown);
                if (title != null) {
                    ExtensionTitle exTitle = new ExtensionTitle(TITLE_ON_COUNTDOWN.get());
                    exTitle.setTitle(title);
                    exTitle.display(p.getPlayer());
                    if (exTitle.enabled)
                        Message.GAME_COUNTDOWN.reply(p.getPlayer(), arena, value.getColor(), p.getPlayer(), new ColoredNumberEntry(title), countdown, arena.getExtension());
                }
                List<Integer> when = PLAY_SOUND_ON_EACH_BROADCAST_WHEN.get();
                if (when.contains(countdown))
                    p.getPlayer().playSound(p.getPlayer().getLocation(), (Sound) PLAY_SOUND_ON_EACH_BROADCAST_SOUND.get(), 1, 1);
            });

            if (countdown == 0) {
                countdownTask.cancel();
                countdown = SpleefXConfig.COUNTDOWN_ON_ENOUGH_PLAYERS.get();
                start();
            }
        }, 20, 20);
    }

    /**
     * Starts the game
     */
    @Override
    public void start() {
        CompletableFuture.runAsync(() -> {
            for (ArenaPlayer player : playerTeams.keySet()) {
                getSpectatorSettings().getSpectatePlayerMenu().playerHeadInfo.apply(player.getPlayer())
                        .thenAccept(head -> playerHeads.put(player.getPlayer().getUniqueId(), head));
            }
        }).thenAcceptAsync((v) -> new SpectatePlayerMenu(arena, true));
        // apparently the first initializing will be slow, so we do it async when it's not
        // needed so other calls would go faster
        setArenaStage(ArenaStage.ACTIVE);
        SpleefX.BSTATS_EXTENSIONS.merge(arena.getExtension().getKey(), 1, (o, a) -> o++);
        currentScoreboard = ScoreboardType.GAME_ACTIVE;
        playerTeams.forEach((arenaPlayer, team) -> {
            alive.add(arenaPlayer.getPlayer());
            broadcasted.add(arenaPlayer.getPlayer().getUniqueId());
            prepareForGame(arenaPlayer, team);
        });
        timeLeft = arena.getGameTime() * 60;
        origTimeLeft = arena.getGameTime() * 60;
        arena.getExtension().getRunCommandsWhenGameStarts().forEach(c -> SenderType.CONSOLE.run(null, c, arena));
        loop();
    }

    /**
     * Runs the game loop
     */
    @Override
    public void loop() {
        if (getArenaStage() != ArenaStage.ACTIVE) return;
        Map<Integer, String> numbers = TIME_OUT_WARN.get();
        try {
            timerTask = Bukkit.getScheduler().runTaskTimer(getPlugin(), () -> {
                timeLeft--;
                String m = numbers.get(timeLeft);
                playerTeams.forEach((p, team) -> {
                    if (DISPLAY_COUNTDOWN_ON_EXP_BAR.get()) {
                        p.getPlayer().setLevel(timeLeft);
                        p.getPlayer().setExp((float) timeLeft / origTimeLeft);
                    }
                    if (m != null)
                        Message.GAME_TIMEOUT.reply(p.getPlayer(), arena, team.getColor(), p.getPlayer(), new ColoredNumberEntry(m), timeLeft, arena.getExtension());
                });
            }, 20, 20);

            gameTask = Bukkit.getScheduler().runTaskTimer(getPlugin(), () -> {
                if (timeLeft == 0)
                    draw();
                if (arena.getArenaType() == ArenaType.FREE_FOR_ALL) {
                    GameTeam ffa = arena.getGameTeams().get(0);
                    alive.stream().filter(player -> player.getLocation().getY() <= arena.getDeathLevel()).forEach(player -> lose(ArenaPlayer.adapt(player), ffa, false));
                    if (alive.size() == 1) {
                        ArenaPlayer p = ArenaPlayer.adapt(alive.get(0));
                        win(p, playerTeams.get(p));
                        end(true);
                        return;
                    }
                } else {
                    arena.getGameTeams().forEach(team -> team.getAlive().forEach((player) -> {
                        if (player.getLocation().getY() <= arena.getDeathLevel())
                            lose(ArenaPlayer.adapt(player), team, false);
                        if (team.getAlive().size() == 0) {
                            toBroadcast().forEach(p -> Message.TEAM_ELIMINATED.reply(p.getPlayer(), arena, team.getColor(), -1, arena.getExtension()));
                            deadTeams.add(team);
                        }
                    }));
                    List<GameTeam> teamsLeft =
                            arena.getGameTeams().stream().filter(team -> !team.isEliminated())
                                    .collect(Collectors.toList());
                    if (teamsLeft.size() == 1) {
                        GameTeam team = teamsLeft.get(0);
                        team.getAlive().forEach(p -> {
                            ArenaPlayer player = ArenaPlayer.adapt(p);
                            win(player, team);
                        });
                        end(true);
                        return;
                    }
                    if (teamsLeft.isEmpty()) {
                        draw();
                        return;
                    }
                }
                if (alive.size() == 0) draw();
            }, ((Integer) ARENA_UPDATE_INTERVAL.get()).longValue(), ((Integer) ARENA_UPDATE_INTERVAL.get()).longValue());
        } catch (ConcurrentModificationException | NullPointerException ignored) {
        }
    }

    /**
     * Ends the game
     */
    @Override
    public void end(boolean giveRewards) {
        endTasks.stream().filter(task -> task.getPhase() == Phase.BEFORE).forEach(GameTask::run);
        if (giveRewards) {
            Collections.reverse(dead);
            Collections.reverse(deadTeams);
            if (arena.getArenaType() == ArenaType.FREE_FOR_ALL) {
                for (Entry<Integer, Map<SenderType, List<String>>> entry : arena.getExtension().getRunCommandsForFFAWinners().entrySet()) {
                    Integer index = entry.getKey();
                    Map<SenderType, List<String>> commandsToRun = entry.getValue();
                    try {
                        ArenaPlayer winner = dead.get(index - 1);
                        if (winner != null) {
                            int sum = betsMap.values().stream().mapToInt(integer -> integer).sum();
                            commandsToRun.forEach((sender, commands) -> commands.forEach(c -> sender.run(winner.getPlayer(), c.replace("{portion}", NUMBER_FORMAT.format(sum)), arena)));
                            if (arena.shouldTakeBets()) {
                                PlayerProfile profile = PlayerRepository.REPOSITORY.lookup(winner);
                                Objects.requireNonNull(profile, "profile");
                                getTracker(winner.getPlayer()).addCoins(sum);
//                                getPlugin().getDataProvider().getStatistics(winner.getPlayer()).giveCoins(winner.getPlayer(), sum);
                                Message.WON_GAME_BET.reply(winner.getPlayer(), arena, winner.getPlayer(), arena.getExtension(), new BetEntry(arena.getBet(), NUMBER_FORMAT.format(sum)));
                            }
                        }
                    } catch (IndexOutOfBoundsException ignored) { // Theres a reward for the 3rd place but there is no 3rd player, etc.
                    }
                }
            } else
                arena.getExtension().getRunCommandsForTeamWinners().forEach((index, commandsToRun) -> {
                    try {
                        GameTeam winningTeam = deadTeams.get(index - 1);
                        if (winningTeam != null) {
                            int portion = betsMap.values().stream().mapToInt(integer -> integer).sum() / arena.getMembersPerTeam();
                            winningTeam.getMembers().forEach(p -> {
                                commandsToRun.forEach((sender, commands) -> commands.forEach(c -> sender.run(p, c.replace("{portion}", NUMBER_FORMAT.format(portion)), arena)));
                                if (arena.shouldTakeBets()) {

                                    PlayerProfile profile = PlayerRepository.REPOSITORY.lookup(p);
                                    PlayerProfile.Builder stats = profile.asBuilder();
                                    stats.addCoins(portion);

                                    Message.WON_GAME_BET.reply(p.getPlayer(), arena, p.getPlayer(), -1, arena.getExtension(), "{portion}", NUMBER_FORMAT.format(portion));
                                }
                            });
                        }
                    } catch (IndexOutOfBoundsException ignored) { // Theres a reward for the 3rd place but there is no 3rd player, etc.
                    }
                });
        }
        statsTrackerMap.values().forEach(tracker -> tracker.applyChanges(arena));
        statsTrackerMap.clear();
        playerTeams.clear();
        betsMap.clear();
        alive.clear();
        dead.clear();
        deadTeams.clear();
        abilityCount.clear();
        spectators.forEach(p -> {
            getPlugin().getSpectatingHandler().disableSpectationMode(p.getPlayer());
            load(p, false);
        });
        arena.getGameTeams().forEach(team -> {
            team.getMembers().clear();
            team.getAlive().clear();
        });
        spectators.clear();
        endTasks.stream().filter(task -> task.getPhase() == Phase.AFTER).forEach(GameTask::run);
        broadcasted.clear();
        regenerate(ArenaStage.WAITING);
        setArenaStage(ArenaStage.WAITING);
    }

    /**
     * Forcibly ends the game (used when the server is shutting down)
     */
    @Override
    public void forceEnd() {
        playerTeams.keySet().forEach(p -> {
            load(p, false);
            if (arena.shouldTakeBets())
                p.getStats().asBuilder().addCoins(betsMap.remove(p.getPlayer()));
            Message.SERVER_STOPPED.reply(p.getPlayer(), arena, p.getPlayer(), arena.getExtension());
        });

        playerHeads.clear();
        end(false);
    }

    /**
     * Regenerates the arena
     *
     * @param newStage Stage to set the arena on once regenerating is over.
     */
    @SneakyThrows @Override
    public void regenerate(@Nullable ArenaStage newStage) {
        ArenaStage oldStage = newStage == null ? getArenaStage() : newStage;
        setArenaStage(ArenaStage.REGENERATING);
        getPlugin().getArenaManager().regenerateArena(arena.getKey()).thenAccept((v) -> {
            setArenaStage(oldStage);
            getSignManager().update();
        });
        currentScoreboard = ScoreboardType.WAITING_IN_LOBBY;
    }

    /**
     * Saves the player data before they enter the arena, such as the inventory and location
     *
     * @param player Player to save for
     */
    @Override
    public void save(ArenaPlayer player) {
        Metas.set(player.getPlayer(), "spleefx.data", new FixedMetadataValue(getPlugin(), new PlayerContext(player.getPlayer())));
    }

    /**
     * Loads the saved data into the player
     *
     * @param p Player to load for
     */
    @Override
    public void load(ArenaPlayer p, boolean delayTick) {
        Runnable load = () -> {
            Player player = p.getPlayer();

            PlayerContext context = Metas.get(player, "spleefx.data");
            if (context != null)
                context.load(player, arena);
            player.removeMetadata("spleefx.data", getPlugin());

            ArenaPlayer.adapt(player).setCurrentArena(null).setState(ArenaPlayerState.NOT_INGAME);
            if (arena.getArenaType() == ArenaType.FREE_FOR_ALL) {
                arena.getFFAManager().remove(player);
            }
            abilityCount.remove(player.getUniqueId());

            if (context != null)
                player.setAllowFlight(context.allowFlight);

            player.setFallDistance(-500);
            getPlugin().getScoreboardTicker().getBoards().remove(player.getUniqueId());
            try {
                player.setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard());
            } catch (NullPointerException ignored) {
            }
        };
        if (delayTick) Bukkit.getScheduler().runTaskLater(SpleefX.getPlugin(), load, 1);
        else load.run();
    }

    /**
     * Displays the game scoreboard for the player
     *
     * @param p Player to display for
     */
    @Override
    public void displayScoreboard(ArenaPlayer p) {
        if (p == null) return;
        Player player = p.getPlayer();
        ScoreboardHolder scoreboard = arena.getExtension().getScoreboard().get(getCurrentScoreboard());
        if (scoreboard == null || !scoreboard.isEnabled()) return;
        scoreboard.createScoreboard(ArenaPlayer.adapt(player));
    }

    /**
     * Returns the scoreboard placeholders map
     *
     * @param player Player to retrieve necessary information from
     * @return The placeholders map
     */
    public Map<String, Supplier<String>> getScoreboardMap(Player player) {
        if (!player.isOnline()) return Collections.emptyMap();
        Map<String, Supplier<String>> map = new HashMap<>();
        map.put("{double_jumps}", () -> Integer.toString(abilityCount.getOrDefault(player.getUniqueId(), DEFAULT_MAP).get(GameAbility.DOUBLE_JUMP)));
        return map;
    }

    /**
     * Sends a message to all the players
     *
     * @param key Message key to broadcast
     */
    @Override
    public void broadcast(Message key, Object... format) {
        playerTeams.forEach((player, team) -> key.reply(player.getPlayer(), arena, team, player.getPlayer(), arena.getExtension()));
    }

    /**
     * Returns the signs manager, which updates all signs accordingly
     *
     * @return The signs manager
     */
    @Override
    public SignManager getSignManager() {
        return signManager;
    }

    /**
     * Returns a map of all arena players assigned to their teams
     *
     * @return ^
     */
    @Override
    public Map<ArenaPlayer, GameTeam> getPlayerTeams() {
        return playerTeams;
    }

    @Override
    public ScoreboardType getCurrentScoreboard() {
        return currentScoreboard;
    }

    /**
     * Returns whether is this arena full or not
     *
     * @return ^
     */
    @Override
    public boolean isFull() {
        return playerTeams.size() == arena.getMaximum();
    }

    /**
     * Registers a task that is ran when the game ends
     *
     * @param task Task to add
     */
    public void registerEndTask(GameTask task) {
        endTasks.add(task);
    }

    public TempStatsTracker getTracker(Player player) {
        return statsTrackerMap.computeIfAbsent(player.getUniqueId(), TempStatsTracker::new);
    }

    /**
     * Returns the ability count of each player
     *
     * @return The ability count map
     */
    @Override
    public Map<UUID, EnumMap<GameAbility, Integer>> getAbilityCount() {
        return abilityCount;
    }

    @Override
    public List<Player> getAlive() {
        return alive;
    }

    @Override public List<GameTeam> getDeadTeams() {
        return deadTeams;
    }

    public List<ArenaPlayer> getTrackedPlayers() {
        List<ArenaPlayer> all = new ArrayList<>(playerTeams.keySet());
        all.addAll(spectators);
        return all;
    }

    public List<Player> toBroadcast() {
        return broadcasted.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).collect(Collectors.toList());
    }

    static {
        for (GameAbility ability : GameAbility.values()) {
            DEFAULT_MAP.put(ability, 0);
        }
    }
}