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
package io.github.spleefx.message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.api.BaseArenaEngine;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.economy.booster.BoosterFactory;
import io.github.spleefx.economy.booster.BoosterInstance;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.extension.standard.splegg.SpleggExtension;
import io.github.spleefx.extension.standard.splegg.SpleggUpgrade;
import io.github.spleefx.perk.GamePerk;
import io.github.spleefx.team.TeamColor;
import io.github.spleefx.util.game.Chat;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.moltenjson.configuration.direct.DirectConfiguration;
import org.moltenjson.json.JsonFile;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.github.spleefx.data.GameStats.FORMAT;

/**
 * A class with all the message keys
 */
public enum MessageKey {

    /* Success */
    ARENA_CREATED("arenaCreated", MessageCategory.SUCCESS, "Arena created", "Sent when an arena is created"),
    ARENA_DELETING("arenaDeleting", MessageCategory.SUCCESS, "Arena deleting", "Sent when an arena is being deleted"),
    ARENA_DELETED("arenaDeleted", MessageCategory.SUCCESS, "Arena deleted", "Sent when an arena has been deleted"),
    ARENA_RENAMED("arenaRenamed", MessageCategory.SUCCESS, "Arena renamed", "Sent when an arena has been renamed"),
    SPAWNPOINT_SET("spawnpointSet", MessageCategory.SUCCESS, "Spawnpoint set", "Sent when a team spawnpoint has been set"),
    LOBBY_SET("lobbySet", MessageCategory.SUCCESS, "Lobby set", "Sent when a lobby is set"),

    /* Error */
    NO_PERMISSION("noPermission", MessageCategory.ERROR, "No permission", "Sent when a player attempts to execute a command but has no permission"),
    NOT_PLAYER("notPlayer", MessageCategory.ERROR, "Executor is not a player", "Sent when a command sender is not a player when required"),
    UNKNOWN_PLAYER("unknownPlayer", MessageCategory.ERROR, "Inputted player is not found", "Sent when the inputted player is either offline or invalid"),
    UNKNOWN_SUBCOMMAND("unknownSubcommand", MessageCategory.ERROR, "Unknown subcommand", "Sent when a player runs an unknown subcommand"),
    TEAM_NOT_REGISTERED("teamNotRegistered", MessageCategory.ERROR, "Team not registered", "Sent when attempting to set a spawnpoint for an invalid team"),
    NOT_IN_ARENA("notInArena", MessageCategory.ERROR, "Not in arena", "Sent when the player attempts to leave an arena but isn't in one"),
    DISALLOWED_COMMAND("disallowedCommand", MessageCategory.ERROR, "Command not allowed", "Sent when a player attempts to send a disallowed command in-game"),
    NO_AVAILABLE_ARENA("noAvailableArena", MessageCategory.ERROR, "No available arena", "Sent when attempting to pick a random arena but none is found"),
    NO_PERMISSION_STATISTICS("noPermissionStats", MessageCategory.ERROR, "No permission (stats)", "Sent when a player attempts to view statistics of other players"),
    NO_ARENAS("noArenas", MessageCategory.ERROR, "No arenas in mode", "Sent in /<mode> listarenas when there are not any arenas"),

    /* Arena */
    ARENA_REGENERATING("arenaRegenerating", MessageCategory.ARENA, "Arena regenerating", "Sent when a player attempts to join a regenerating arena"),
    ARENA_NEEDS_SETUP("arenaNeedsSetup", MessageCategory.ARENA, "Arena needs setup", "Sent when a player attempts to join an unplayable arena"),
    ARENA_ALREADY_ACTIVE("arenaAlreadyActive", MessageCategory.ARENA, "Arena active", "Sent when a pleyer attempts to join an active arena"),
    ARENA_FULL("arenaFull", MessageCategory.ARENA, "Arena full", "Sent when a pleyer attempts to join a full arena"),
    ARENA_DISABLED("arenaDisabled", MessageCategory.ARENA, "Arena disabled", "Sent when a player attempts to join a disabled arena"),
    MUST_HAVE_EMPTY_INV("mustHaveEmptyInventory", MessageCategory.ARENA, "Must have an empty inventory", "Sent when a player attempts to join with a non-empty inventory when it is required."),
    ARENA_ALREADY_EXISTS("arenaAlreadyExists", MessageCategory.ARENA, "Arena already exists", "Sent when attempting to create an arena with an existing key"),
    INVALID_ARENA("invalidArena", MessageCategory.ARENA, "Invalid arena", "Sent when the requested arena is invalid"),
    SERVER_STOPPED("serverStopped", MessageCategory.ARENA, "Server stopped", "Sent to in-game players when the server stops"),

    NOT_ENOUGH_PLAYERS("notEnoughPlayers", MessageCategory.ARENA, "Not enough players to start the game", "Broadcasted when a player leaves and the minimum player count is not met"),
    GAME_COUNTDOWN("gameCountdown", MessageCategory.ARENA, "Game countdown", "Broadcasted when an arena is counting down"),
    GAME_TIMEOUT("gameTimeout", MessageCategory.ARENA, "Game time out", "Broadcasted when the game is about to time out"),
    GAME_STARTING("gameStarting", MessageCategory.ARENA, "Game starting", "Broadcasted when there are enough players to start the game"),

    /* Arena teams */
    PLAYER_JOINED_T("playerJoined", MessageCategory.ARENA_TEAMS, "Player joined the game", "Broadcasted when a player joins the arena"),
    TEAM_ELIMINATED("teamEliminated", MessageCategory.ARENA_TEAMS, "Team eliminated", "Broadcasted when a team has been eliminated"),
    PLAYER_LOST_T("playerLost", MessageCategory.ARENA_TEAMS, "Player loses", "Broadcasted when a player loses"),
    PLAYER_WINS_T("playerWins", MessageCategory.ARENA_TEAMS, "Player wins", "Broadcasted when a player wins"),

    /* Arena FFA */
    PLAYER_JOINED_FFA("playerJoined", MessageCategory.ARENA_FFA, "Player joined the game", "Broadcasted when a player joins the arena"),
    PLAYER_LOST_FFA("playerLost", MessageCategory.ARENA_FFA, "Player loses", "Broadcasted when a player loses"),
    PLAYER_WINS_FFA("playerWins", MessageCategory.ARENA_FFA, "Player wins", "Broadcasted when a player wins"),

    /* Stages */
    WAITING("waiting", MessageCategory.STAGES, "Waiting stage", "Displayed on sign when an arena is waiting for players"),
    COUNTDOWN("countdown", MessageCategory.STAGES, "Countdown stage", "Displayed on sign when an arena is counting down to start"),
    ACTIVE("active", MessageCategory.STAGES, "Active stage", "Displayed on sign when an arena is active"),
    REGENERATING("regenerating", MessageCategory.STAGES, "Regenerating stage", "Displayed on sign when an arena is regenerating"),
    NEEDS_SETUP("needsSetup", MessageCategory.STAGES, "Needs setup", "Displayed on sign when an arena has not been fully setup"),
    DISABLED("disabled", MessageCategory.STAGES, "Arena mode disabled", "Displayed on sign when a mode is disabled"),

    /* Teams */
    FFA_COLOR("ffa", MessageCategory.TEAMS, "FFA Color", "Displayed in chat for FFA games as each player's color"),
    RED("red", MessageCategory.TEAMS, "Red team", "Displayed in chat when representing the red team"),
    GREEN("green", MessageCategory.TEAMS, "Green team", "Displayed in chat when representing the green team"),
    BLUE("blue", MessageCategory.TEAMS, "Blue team", "Displayed in chat when representing the blue team"),
    YELLOW("yellow", MessageCategory.TEAMS, "Yellow team", "Displayed in chat when representing the yellow team"),
    PINK("pink", MessageCategory.TEAMS, "Pink team", "Displayed in chat when representing the pink team"),
    GRAY("gray", MessageCategory.TEAMS, "Gray team", "Displayed in chat when representing the gray team"),

    /* Bets messages */
    BET_TAKEN("betTaken", MessageCategory.BETS, "Bet taken", "Sent to the player when they participate in an arena that takes bets"),
    WON_GAME_BET("wonGameBet", MessageCategory.BETS, "Won the game bet", "Sent when the player wins the bet (or a part of it in case of teams) from a game"),
    NOT_ENOUGH_TO_BET("notEnoughToBet", MessageCategory.BETS, "Not enough money to bet", "Sent when a player attempts to join an arena which requires betting, but does not have enough money")

    /* Economy */,
    MONEY_GIVEN("moneyGiven", MessageCategory.ECONOMY, "Money given", "Sent when a player is given money"),
    MONEY_TAKEN("moneyTaken", MessageCategory.ECONOMY, "Money taken", "Sent when money is taken from a player"),
    BOOSTER_GIVEN("boosterGiven", MessageCategory.ECONOMY, "Booster given", "Sent when a player is given a booster"),
    BOOSTER_ACTIVATED("boosterActivated", MessageCategory.ECONOMY, "Booster activated", "Sent when a player activates their booster"),
    CANNOT_ACTIVATE_MORE("cannotActivateMoreBoosters", MessageCategory.ECONOMY, "Can't activate more boosters", "Sent when a player attempts to activate a booster but have already reached the max limit."),
    BOOSTER_ALREADY_ACTIVE("boosterAlreadyActive", MessageCategory.ECONOMY, "Booster already activated", "Sent when a player attempts to activate an already-activated booster."),
    BOOSTER_PAUSED("boosterPaused", MessageCategory.ECONOMY, "Booster paused", "Sent when a player pauses one of their boosters"),
    ITEM_PURCHASED("itemPurchased", MessageCategory.ECONOMY, "Item purchased", "Sent when a player purchases an item successfully"),
    NOT_ENOUGH_COINS("notEnoughCoins", MessageCategory.ECONOMY, "Not enough coins", "Sent when a player tries to purchase an item but does not have enough coins."),
    ALREADY_PURCHASED("alreadyPurchased", MessageCategory.ECONOMY, "Item already purchased", "Sent when a player tries to purchase an item but they already have it."),

    /* Splegg upgrades */
    UPGRADE_SELECTED("upgradeSelected", MessageCategory.SPLEGG_UPGRADES, "Upgrade selected", "Sent when a player selects a splegg upgrade."),
    NOT_ENOUGH_COINS_SPLEGG("notEnoughCoinsSplegg", MessageCategory.SPLEGG_UPGRADES, "Not enough coins", "Sent when a player tries to purchase an upgrade but does not have enough coins."),
    UPGRADE_PURCHASED("upgradePurchased", MessageCategory.SPLEGG_UPGRADES, "Upgrade purchased", "Sent when a player successfully purchases a splegg upgrade."),
    MUST_PURCHASE_BEFORE("mustPurchaseBefore", MessageCategory.SPLEGG_UPGRADES, "Must purchase before", "Sent when a player tries to purchase an upgrade but hasn't unlocked the ones required first.");

    /**
     * The message key
     */
    private String key;

    /**
     * The message category
     */
    private MessageCategory category;

    private String message;

    private String name;

    private String description;

    MessageKey(String key, MessageCategory category, String name, String description) {
        this.key = key;
        this.category = category;
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the literal text of the message
     *
     * @return The message text
     */
    public String getText() {
        return message == null ? (message = MESSAGES_MAP.get(category).get(key)) : message;
    }

    /**
     * Returns the display name of the key
     *
     * @return The display name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the message
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sends the message by applying all placeholders. Null values will not get replaced
     *
     * @param sender         The entity to receive the message
     * @param arena          The game arena. Can be null
     * @param team           The team. Can be null
     * @param location       The location. Can be null
     * @param player         The player. Can be null
     * @param command        The command. Can be null
     * @param countdown      The countdown. Can be null
     * @param countdownValue The string representation of the countdown. Can be null
     * @param extension      The extension to get prefix from. Can be null
     */
    public void send(CommandSender sender, GameArena arena, TeamColor team, Location location, Player player, String command, String countdown, int countdownValue, GameExtension extension, Object... replacements) {
        String message = getText();
        if (message == null || message.equals("{}")) return;
        if (replacements != null && replacements.length != 0 && replacements.length % 2 == 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                Object key = replacements[i];
                Object value = replacements[i + 1];
                message = message.replace(key == null ? "" : key.toString(), value == null ? "" : value.toString());
            }
        }
        if (arena != null) {
            message = message
                    .replace("{arena}", arena.getKey())
                    .replace("{arena_displayname}", arena.getDisplayName())
                    .replace("{arena_bet}", FORMAT.format(arena.getBet()))
                    .replace("{arena_time_left}", Integer.toString(((BaseArenaEngine<? extends GameArena>) arena.getEngine()).timeLeft))
                    .replace("{arena_playercount}", Integer.toString(arena.getEngine().getPlayerTeams().size()))
                    .replace("{arena_minimum}", Integer.toString(arena.getMinimum()))
                    .replace("{arena_maximum}", Integer.toString(arena.getMaximum()))
                    .replace("{arena_stage}", arena.getEngine().getArenaStage().getState());
        }
        if (team != null) {
            message = message
                    .replace("{team}", team.chat())
                    .replace("{team_color}", team.getChatColor());
        }
        if (location != null) {
            message = message
                    .replace("{x}", Double.toString(location.getX()))
                    .replace("{y}", Double.toString(location.getY()))
                    .replace("{z}", Double.toString(location.getZ()));
        }
        if (player != null) {
            message = message.replace("{player}", player.getName());
        }
        if (command != null) {
            message = message.replace("{command}", command);
        }
        if (countdownValue != -1) {
            message = message.replace("{countdown}", countdown == null ? Integer.toString(countdownValue) : countdown)
                    .replace("{plural}", countdownValue == 1 ? "" : "s");
        }
        if (extension != null) {
            message = message
                    .replace("{extension_key}", extension.getKey())
                    .replace("{extension_chat_prefix}", extension.getChatPrefix())
                    .replace("{extension}", extension.getDisplayName());
        }
        if (PAPI) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }
        Chat.prefix(sender, extension, message);
    }

    public static String formatTime(int seconds) {
        int secondsLeft = seconds % 3600 % 60;
        int minutes = (int) Math.floor((float) seconds % 3600 / 60);
        int hours = (int) Math.floor((float) seconds / 3600);

        String hoursFormat = ((hours < 10) ? "0" : "") + hours;
        String minutesFormat = ((minutes < 10) ? "0" : "") + minutes;
        String secondsFormat = ((secondsLeft < 10) ? "0" : "") + secondsLeft;
        if (hours <= 0)
            return minutesFormat + ":" + secondsFormat;
        return hoursFormat + ":" + minutesFormat + ":" + secondsFormat;
    }

    public void sendBooster(CommandSender sender, BoosterInstance booster) {
        String message = getText();
        if (message.equals("{}")) return;
        BoosterFactory type = booster.getType();
        message = message
                .replace("{booster_limit}", Integer.toString(BoosterFactory.ALLOW_MULTIPLE.get()))
                .replace("{booster_type_displayname}", type.getDisplayName())
                .replace("{booster_type}", type.getDisplayName()) // fallback lol
                .replace("{booster_type_key}", type.getKey())
                .replace("{duration}", type.getDuration().toString())
                .replace("{booster_multiplier}", Double.toString(booster.getMultiplier()))
                .replace("{booster_time_left}", Long.toString(booster.getDuration()))
                .replace("{booster_type_duration}", type.getDuration().toString())
                .replace("{booster_is_active}", booster.isActive() ? "&cActive" : "&aAvailable");
        Chat.plugin(sender, message);
    }

    public void sendPerk(CommandSender sender, GamePerk perk) {
        String message = getText();
        if (message.equals("{}")) return;
        message = message.replace("{perk_key}", perk.getKey())
                .replace("{perk_displayname}", perk.getDisplayName())
                .replace("{perk_usable_amount}", Integer.toString(perk.getPurchaseSettings().getGamesUsableFor())
                        .replace("{perk_ingame_amount}", Integer.toString(perk.getPurchaseSettings().getIngameAmount())));
        Chat.plugin(sender, message);
    }

    public void sendSpleggUpgrade(CommandSender sender, SpleggUpgrade upgrade) {
        String message = getText();
        if (message.equals("{}")) return;
        message = message
                .replace("{upgrade_key}", upgrade.getKey())
                .replace("{upgrade_displayname}", upgrade.getDisplayName())
                .replace("{upgrade_price}", FORMAT.format(upgrade.getPrice()))
                .replace("{upgrade_delay}", Double.toString(upgrade.getDelay()));
        Chat.prefix(sender, SpleggExtension.EXTENSION, message);
    }

    /**
     * Sets the key's text
     *
     * @param text New text to set
     */
    public void setText(String text) {
        MESSAGES_MAP.get(category).put(key, text);
        message = text;
    }

    /**
     * The messages.json file
     */
    private static final DirectConfiguration MESSAGES_CONFIG = DirectConfiguration.of(JsonFile.of(SpleefX.getPlugin().getDataFolder(), "messages.json"));

    /**
     * A map that contains all messages mapped to their keys
     */
    private static final Map<MessageCategory, Map<String, String>> MESSAGES_MAP = new HashMap<>();

    /**
     * The reflective type of maps
     */
    private static final Type MAP_TYPE = new TypeToken<HashMap<String, String>>() {
    }.getType();

    /**
     * Gson used for reading and writing
     */
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    /**
     * Maps all categories by their messages
     */
    private static final Map<MessageCategory, List<MessageKey>> BY_CATEGORY = new HashMap<>();

    /**
     * Whether is PlaceholderAPI present or not
     */
    public static final boolean PAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

    /**
     * The chat prefix
     */
    private static String PREFIX;

    /**
     * Saves the messages to the config
     */
    public static void save() {
        MESSAGES_MAP.forEach((category, messages) -> MESSAGES_CONFIG.set(category.name().toLowerCase(), messages, GSON));
        MESSAGES_CONFIG.save(Throwable::printStackTrace, GSON);
    }

    /**
     * Returns the chat prefix
     *
     * @return The prefix
     */
    public static String prefix() {
        return PREFIX;
    }

    public static void load(boolean reload) {
        if (reload) MESSAGES_CONFIG.refresh();
        PREFIX = Chat.colorize(MESSAGES_CONFIG.getString("globalPrefix"));
        for (MessageCategory category : MessageCategory.values()) {
            Map<String, String> map = MESSAGES_CONFIG.get(category.name().toLowerCase(), MAP_TYPE);
            map.replaceAll((key, value) -> Chat.colorize(value));
            MESSAGES_MAP.put(category, map);
        }
        for (MessageCategory category : MessageCategory.values()) {
            BY_CATEGORY.put(category, new LinkedList<>());
        }
        for (MessageKey key : values()) {
            BY_CATEGORY.get(key.category).add(key);
        }
    }

    /**
     * Returns all the message keys that are in a category
     *
     * @param category Category to get from
     * @return A list of all keys in that category
     */
    public static List<MessageKey> byCategory(MessageCategory category) {
        return BY_CATEGORY.get(category);
    }
}