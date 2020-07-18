package io.github.spleefx.util.message.message;

import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.util.PlaceholderUtil;
import io.github.spleefx.util.game.Chat;
import lombok.EqualsAndHashCode;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Stream;

/**
 * Represents a message. Construct with {@link MessageBuilder}.
 */
@EqualsAndHashCode
public class Message implements Iterable<Message> {

    @MessageMapping("prefix")
    public static final Message PREFIX = new MessageBuilder("Prefix")
            .describe("The prefix that comes before any message. To send a message without prefix, precede it with '[noprefix]'.")
            .defaultTo("&7[&bSpleef&fX&7] ").build();

    @MessageMapping("success.arenaCreated")
    public static final Message ARENA_CREATED = new MessageBuilder("Arena.Created")
            .describe("Sent when an arena is created")
            .defaultTo("&aArena &e{arena} &ahas been created. Run &e/{command} arena settings {arena}&a.")
            .build();

    @MessageMapping("success.arenaDeleting")
    public static final Message ARENA_DELETING = new MessageBuilder("Arena.Deleting")
            .describe("Sent when an arena is being deleted")
            .defaultTo("&cDeleting &e{arena}&c...")
            .build();

    @MessageMapping("success.arenaDeleted")
    public static final Message ARENA_DELETED = new MessageBuilder("Arena.Deleted")
            .describe("Sent when an arena has been deleted")
            .defaultTo("&aArena &e{arena} &ahas been deleted")
            .build();

    @MessageMapping("success.arenaRenamed")
    public static final Message ARENA_RENAMED = new MessageBuilder("Arena.Renamed")
            .describe("Sent when an arena has been renamed")
            .defaultTo("&aDisplay name of arena &e{arena} &ahas been changed into &d{arena_displayname}")
            .build();

    @MessageMapping("success.spawnpointSet")
    public static final Message SPAWNPOINT_SET = new MessageBuilder("Arena.SpawnpointSet")
            .describe("Sent when a team spawnpoint has been set")
            .defaultTo("&aTeam {team}&a's spawnpoint has been set to &e{x}&a, &e{y}&a, &e{z}&a.")
            .build();

    @MessageMapping("success.lobbySet")
    public static final Message LOBBY_SET = new MessageBuilder("Arena.LobbySet")
            .describe("Sent when a lobby is set")
            .defaultTo("&aArena &e{arena}&a's lobby has been set to &e{x}&a, &e{y}&a, &e{z}&a.")
            .build();

    @MessageMapping("error.noPermission")
    public static final Message NO_PERMISSION = new MessageBuilder("Command.NoPermission")
            .describe("Sent when a player attempts to execute a command but has no permission")
            .defaultTo("&cYou do not have permission to perform this command!")
            .build();

    @MessageMapping("error.notPlayer")
    public static final Message NOT_PLAYER = new MessageBuilder("Command.NotPlayer")
            .describe("Sent when a command sender is not a player when required")
            .defaultTo("&cYou must be a player to use this command!")
            .build();

    @MessageMapping("error.unknownPlayer")
    public static final Message UNKNOWN_PLAYER = new MessageBuilder("Command.InvalidPlayer")
            .describe("Sent when the inputted player is either offline or invalid")
            .defaultTo("&cPlayer &e{player} &cis offline or invalid.")
            .build();

    @MessageMapping("error.unknownSubcommand")
    public static final Message UNKNOWN_SUBCOMMAND = new MessageBuilder("Command.InvalidSubcommand")
            .describe("Sent when a player runs an unknown subcommand")
            .defaultTo("&cUnrecognizable subcommand. Try &e/{command} help&c.")
            .build();

    @MessageMapping("error.teamNotRegistered")
    public static final Message TEAM_NOT_REGISTERED = new MessageBuilder("Command.TeamNotRegistered")
            .describe("Sent when attempting to set a spawnpoint for an invalid team")
            .defaultTo("&cTeam {team} &cis not registered in arena &e{arena}&c. Add the team in &e/spleef arena settings {arena}&c.")
            .build();

    @MessageMapping("error.notInArena")
    public static final Message NOT_IN_ARENA = new MessageBuilder("Arena.CannotLeave")
            .describe("Sent when the player attempts to leave an arena but isn't in one")
            .defaultTo("&cYou must be in an arena to leave!")
            .build();

    @MessageMapping("error.disallowedCommand")
    public static final Message DISALLOWED_COMMAND = new MessageBuilder("Game.CommandNotAllowed")
            .describe("Sent when a player attempts to send a disallowed command in-game")
            .defaultTo("&cYou may not execute this command while in-game!")
            .build();

    @MessageMapping("error.noAvailableArena")
    public static final Message NO_AVAILABLE_ARENA = new MessageBuilder("PlayerCannotJoin.NoAvailableArenas")
            .describe("Sent when attempting to pick a random arena but none is found")
            .defaultTo("&cI couldn't find an available arena for you")
            .build();

    @MessageMapping("error.noPermissionStats")
    public static final Message NO_PERMISSION_STATISTICS = new MessageBuilder("Command.NoPermissionToViewStats")
            .describe("Sent when a player attempts to view statistics of other players")
            .defaultTo("&cYou don't have permission to view statistics of other players!")
            .build();

    @MessageMapping("error.noArenas")
    public static final Message NO_ARENAS = new MessageBuilder("Command.NoArenasInMode")
            .describe("Sent in /<mode> listarenas when there are not any arenas")
            .defaultTo("&cThis game type has no arenas!")
            .build();

    @MessageMapping("arena.arenaRegenerating")
    public static final Message ARENA_REGENERATING = new MessageBuilder("PlayerCannotJoin.ArenaRegenerating")
            .describe("Sent when a player attempts to join a regenerating arena")
            .defaultTo("&cPlease wait while this arena regenerates.")
            .build();

    @MessageMapping("arena.arenaNeedsSetup")
    public static final Message ARENA_NEEDS_SETUP = new MessageBuilder("PlayerCannotJoin.ArenaNotReady")
            .describe("Sent when a player attempts to join an unplayable arena")
            .defaultTo("&cThis arena is not in a playable state.")
            .build();

    @MessageMapping("arena.arenaAlreadyActive")
    public static final Message ARENA_ALREADY_ACTIVE = new MessageBuilder("PlayerCannotJoin.ArenaAlreadyActive")
            .describe("Sent when a pleyer attempts to join an active arena")
            .defaultTo("&cThis arena is already active!")
            .build();

    @MessageMapping("arena.arenaFull")
    public static final Message ARENA_FULL = new MessageBuilder("PlayerCannotJoin.ArenaFull")
            .describe("Sent when a pleyer attempts to join a full arena")
            .defaultTo("&cThis arena is full!")
            .build();

    @MessageMapping("arena.arenaDisabled")
    public static final Message ARENA_DISABLED = new MessageBuilder("PlayerCannotJoin.ArenaDisabled")
            .describe("Sent when a player attempts to join a disabled arena")
            .defaultTo("&cThis arena is disabled")
            .build();

    @MessageMapping("arena.mustHaveEmptyInventory")
    public static final Message MUST_HAVE_EMPTY_INV = new MessageBuilder("PlayerCannotJoin.MustHaveEmptyInventory")
            .describe("Sent when a player attempts to join with a non-empty inventory when it is required.")
            .defaultTo("&cYou must have an empty inventory in order to join!")
            .build();

    @MessageMapping("arena.arenaAlreadyExists")
    public static final Message ARENA_ALREADY_EXISTS = new MessageBuilder("Arena.AlreadyExists")
            .describe("Sent when attempting to build an arena with an existing key")
            .defaultTo("&cAn arena with the key &e{arena} &calready exists! Remove it with &e/spleef arena remove {arena}&c.")
            .build();

    @MessageMapping("arena.invalidArena")
    public static final Message INVALID_ARENA = new MessageBuilder("Command.InvalidArena")
            .describe("Sent when the requested arena is invalid")
            .defaultTo("&cNo arena with key &e{arena} &cexists!")
            .build();

    @MessageMapping("arena.serverStopped")
    public static final Message SERVER_STOPPED = new MessageBuilder("Game.ForciblyEndingServerStopped")
            .describe("Sent to in-game players when the server stops")
            .defaultTo("&cThe server has stopped, so the game has been forcibly ended.")
            .build();

    @MessageMapping("arena.notEnoughPlayers")
    public static final Message NOT_ENOUGH_PLAYERS = new MessageBuilder("Game.CountdownCancelledNotEnoughPlayers")
            .describe("Broadcasted when a player leaves and the minimum player count is not met")
            .defaultTo("&eThere are not enough players to start the game. Countdown cancelled.")
            .build();

    @MessageMapping("arena.gameCountdown")
    public static final Message GAME_COUNTDOWN = new MessageBuilder("Game.Countdown")
            .describe("Broadcasted when an arena is counting down")
            .defaultTo("&aGame starting in &e{colored_number} &asecond{plural}")
            .build();

    @MessageMapping("arena.gameTimeout")
    public static final Message GAME_TIMEOUT = new MessageBuilder("Game.TimeOver")
            .describe("Broadcasted when the game is about to time out")
            .defaultTo("&7The game ends in {colored_number} &7second{plural}")
            .build();

    @MessageMapping("arena.gameStarting")
    public static final Message GAME_STARTING = new MessageBuilder("Game.Starting")
            .describe("Broadcasted when there are enough players to start the game")
            .defaultTo("&aThere are enough players to start the game. Starting in &e{value} second{plural}&a.")
            .build();

    @MessageMapping("arena.alreadyInArena")
    public static final Message ALREADY_IN_ARENA = new MessageBuilder("Arena.CannotJoinAlreadyInArena")
            .describe("Sent when the player attempts to join an arena but is in one already.")
            .defaultTo("null")
            .build();

    @MessageMapping("arena_teams.playerJoined")
    public static final Message PLAYER_JOINED_T = new MessageBuilder("Game.PlayerJoined")
            .describe("Broadcasted when a player joins the arena")
            .defaultTo("&e{player} &ahas joined the game - {team} Team&a! &7(&9{arena_playercount}&c/&9{arena_maximum}&7)")
            .build();

    @MessageMapping("arena_teams.teamEliminated")
    public static final Message TEAM_ELIMINATED = new MessageBuilder("Game.TeamEliminated")
            .describe("Broadcasted when a team has been eliminated")
            .defaultTo("&eTeam {team} &ehas been eliminated!")
            .build();

    @MessageMapping("arena_teams.playerLost")
    public static final Message PLAYER_LOST_T = new MessageBuilder("Game.PlayerLost")
            .describe("Broadcasted when a player loses")
            .defaultTo("&c{player} &chas been eliminated!")
            .build();

    @MessageMapping("arena_teams.playerWins")
    public static final Message PLAYER_WINS_T = new MessageBuilder("Game.PlayerWins")
            .describe("Broadcasted when a player wins")
            .defaultTo("{team_color}{player} &ahas won!")
            .build();

    @MessageMapping("arena_ffa.playerJoined")
    public static final Message PLAYER_JOINED_FFA = new MessageBuilder("Game.PlayerJoinedFFA")
            .describe("Broadcasted when a player joins the arena")
            .defaultTo("&e{player} &ahas joined the game! &7(&9{arena_playercount}&c/&9{arena_maximum}&7)")
            .build();

    @MessageMapping("arena_ffa.playerLost")
    public static final Message PLAYER_LOST_FFA = new MessageBuilder("Game.PlayerLostFFA")
            .describe("Broadcasted when a player loses")
            .defaultTo("&c{player} &chas been eliminated!")
            .build();

    @MessageMapping("arena_ffa.playerWins")
    public static final Message PLAYER_WINS_FFA = new MessageBuilder("Game.PlayerWonFFA")
            .describe("Broadcasted when a player wins")
            .defaultTo("{player} &ahas won!")
            .build();

    @MessageMapping("stages.waiting")
    public static final Message WAITING = new MessageBuilder("Stage.Waiting")
            .describe("Displayed on sign when an arena is waiting for players")
            .defaultTo("&5Waiting")
            .build();

    @MessageMapping("stages.countdown")
    public static final Message COUNTDOWN = new MessageBuilder("Stage.Countdown")
            .describe("Displayed on sign when an arena is counting down to start")
            .defaultTo("&1Starting")
            .build();

    @MessageMapping("stages.active")
    public static final Message ACTIVE = new MessageBuilder("Stage.Active")
            .describe("Displayed on sign when an arena is active")
            .defaultTo("&4Running")
            .build();

    @MessageMapping("stages.regenerating")
    public static final Message REGENERATING = new MessageBuilder("Stage.Regenerating")
            .describe("Displayed on sign when an arena is regenerating")
            .defaultTo("&2Regenerating")
            .build();

    @MessageMapping("stages.needsSetup")
    public static final Message NEEDS_SETUP = new MessageBuilder("Stage.NeedsSetup")
            .describe("Displayed on sign when an arena has not been fully setup")
            .defaultTo("&cNeeds setup")
            .build();

    @MessageMapping("stages.disabled")
    public static final Message DISABLED = new MessageBuilder("Stage.Disabled")
            .describe("Displayed on sign when a mode is disabled")
            .defaultTo("&cDisabled")
            .build();

    @MessageMapping("teams.ffa")
    public static final Message FFA_COLOR = new MessageBuilder("TeamColors.FFA")
            .describe("Displayed in chat for FFA games as each player's color")
            .defaultTo("&8")
            .build();

    @MessageMapping("teams.red")
    public static final Message RED = new MessageBuilder("TeamColors.Red")
            .describe("Displayed in chat when representing the red team")
            .defaultTo("&cRed")
            .build();

    @MessageMapping("teams.green")
    public static final Message GREEN = new MessageBuilder("TeamColors.Green")
            .describe("Displayed in chat when representing the green team")
            .defaultTo("&aGreen")
            .build();

    @MessageMapping("teams.blue")
    public static final Message BLUE = new MessageBuilder("TeamColors.Blue")
            .describe("Displayed in chat when representing the blue team")
            .defaultTo("&9Blue")
            .build();

    @MessageMapping("teams.yellow")
    public static final Message YELLOW = new MessageBuilder("TeamColors.Yellow")
            .describe("Displayed in chat when representing the yellow team")
            .defaultTo("&eYellow")
            .build();

    @MessageMapping("teams.pink")
    public static final Message PINK = new MessageBuilder("TeamColors.Pink")
            .describe("Displayed in chat when representing the pink team")
            .defaultTo("&dPink")
            .build();

    @MessageMapping("teams.gray")
    public static final Message GRAY = new MessageBuilder("TeamColors.Gray")
            .describe("Displayed in chat when representing the gray team")
            .defaultTo("&8Gray")
            .build();

    @MessageMapping("bets.betTaken")
    public static final Message BET_TAKEN = new MessageBuilder("Bets.BetTaken")
            .describe("Sent to the player when they participate in an arena that takes bets")
            .defaultTo("&e{arena_bet}$ &ahas been taken from you as a bet")
            .build();

    @MessageMapping("bets.wonGameBet")
    public static final Message WON_GAME_BET = new MessageBuilder("Bets.BetWon")
            .describe("Sent when the player wins the bet (or a part of it in case of teams) from a game")
            .defaultTo("&aYou've won &e{portion}$ &afrom the arena bets!")
            .build();

    @MessageMapping("bets.notEnoughToBet")
    public static final Message NOT_ENOUGH_TO_BET = new MessageBuilder("Bets.NotEnoughMoneyToBet")
            .describe("Sent when a player attempts to join an arena which requires betting, but does not have enough money")
            .defaultTo("&cYou are required to have at least &e{arena_bet} &cas a betting to join the arena.")
            .build();

    @MessageMapping("economy.moneyGiven")
    public static final Message MONEY_GIVEN = new MessageBuilder("Economy.MoneyGiven")
            .describe("Sent when a player is given money")
            .defaultTo("&aYou have been given &e${colored_number}&a.")
            .build();

    @MessageMapping("economy.moneyTaken")
    public static final Message MONEY_TAKEN = new MessageBuilder("Economy.MoneyTaken")
            .describe("Sent when money is taken from a player")
            .defaultTo("&e${colored_number} &ahas been taken from you.")
            .build();

    @MessageMapping("economy.boosterGiven")
    public static final Message BOOSTER_GIVEN = new MessageBuilder("Economy.BoosterGiven")
            .describe("Sent when a player is given a booster")
            .defaultTo("&aYou have been given a booster of type &e{booster_type_displayname}")
            .build();

    @MessageMapping("economy.boosterActivated")
    public static final Message BOOSTER_ACTIVATED = new MessageBuilder("Economy.BoosterActivated")
            .describe("Sent when a player activates their booster")
            .defaultTo("&aYour booster will be activated for &e{duration}&a.")
            .build();

    @MessageMapping("economy.cannotActivateMoreBoosters")
    public static final Message CANNOT_ACTIVATE_MORE = new MessageBuilder("Economy.CannotActivateMoreBoosters")
            .describe("Sent when a player attempts to activate a booster but have already reached the max limit.")
            .defaultTo("&cYou have reached the maximum amount of boosters activated!")
            .build();

    @MessageMapping("economy.boosterAlreadyActive")
    public static final Message BOOSTER_ALREADY_ACTIVE = new MessageBuilder("Economy.BoosterAlreadyActive")
            .describe("Sent when a player attempts to activate an already-activated booster.")
            .defaultTo("&cThis booster is already active!")
            .build();

    @MessageMapping("economy.boosterPaused")
    public static final Message BOOSTER_PAUSED = new MessageBuilder("Economy.BoosterPaused")
            .describe("Sent when a player pauses one of their boosters")
            .defaultTo("&aThis booster has been paused.")
            .build();

    @MessageMapping("economy.itemPurchased")
    public static final Message ITEM_PURCHASED = new MessageBuilder("Economy.ItemPurchased")
            .describe("Sent when a player purchases an item successfully")
            .defaultTo("&aYou have successfully bought {perk_displayname}&a!")
            .build();

    @MessageMapping("economy.notEnoughCoins")
    public static final Message NOT_ENOUGH_COINS = new MessageBuilder("Economy.NotEnoughCoins")
            .describe("Sent when a player tries to purchase an item but does not have enough coins.")
            .defaultTo("&cYou do not have enough coins to purchase this item!")
            .build();

    @MessageMapping("economy.alreadyPurchased")
    public static final Message PERK_ALREADY_PURCHASED = new MessageBuilder("Economy.ItemAlreadyPurchased")
            .describe("Sent when a player tries to purchase an item but they already have it.")
            .defaultTo("&cYou have already purchased this item!")
            .build();

    @MessageMapping("splegg_upgrades.upgradeSelected")
    public static final Message UPGRADE_SELECTED = new MessageBuilder("SpleggUpgrades.UpgradeSelected")
            .describe("Sent when a player selects a splegg upgrade.")
            .defaultTo("&aYou have selected &e{upgrade_displayname}&a.")
            .build();

    @MessageMapping("splegg_upgrades.notEnoughCoinsSplegg")
    public static final Message NOT_ENOUGH_COINS_SPLEGG = new MessageBuilder("SpleggUpgrades.NotEnoughCoins")
            .describe("Sent when a player tries to purchase an upgrade but does not have enough coins.")
            .defaultTo("&cYou do not have enough coins to purchase this upgrade!")
            .build();

    @MessageMapping("splegg_upgrades.upgradePurchased")
    public static final Message UPGRADE_PURCHASED = new MessageBuilder("SpleggUpgrade.UpgradePurchased")
            .describe("Sent when a player successfully purchases a splegg upgrade.")
            .defaultTo("&aSuccessfully purchased and selected &e{upgrade_displayname}&a!")
            .build();

    @MessageMapping("splegg_upgrades.mustPurchaseBefore")
    public static final Message MUST_PURCHASE_BEFORE = new MessageBuilder("SpleggUpgrade.MustPurchaseBefore")
            .describe("Sent when a player tries to purchase an upgrade but hasn't unlocked the ones required first.")
            .defaultTo("&cYou must purchase previous abilities before buying this!")
            .build();

    private final String key;
    private String defaultValue;
    private String comment;
    private String[] description;

    private String value;

    Message(String key, String defaultValue, String comment, String[] description) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.comment = comment;
        this.description = description;
        SpleefX.getPlugin().getMessageManager().registerMessage(this);
    }

    public String build(boolean flatten, Object... formats) {
        String value = getValue();
        if (!value.contains("[noprefix]")) {
            for (Object f : formats) {
                if (f instanceof GameExtension) {
                    value = "[noprefix]" + ((GameExtension) f).getChatPrefix() + value;
                    break;
                }
                if (f instanceof GameArena) {
                    value = "[noprefix]" + ((GameArena) f).getExtension().getChatPrefix() + value;
                    break;
                }
            }
        }
        return PlaceholderUtil.all(value, flatten ? flatten(formats).toArray() : formats);
    }

    public String create(Object... formats) {
        return build(true, formats);
    }

    /**
     * Flattens the specified array by joining all nested arrays to a single array.
     *
     * @param array The array to flatten
     * @return A stream of the flattened array.
     */
    public static Stream<Object> flatten(Object[] array) {
        return Arrays.stream(array).flatMap(o -> o instanceof Object[] ? flatten((Object[]) o) : Stream.of(o));
    }

    public String getValue() {
        return value == null ? value = defaultValue : value;
    }

    public String getKey() {
        return key;
    }

    public String getComment() {
        return comment;
    }

    public String[] getDescription() {
        return description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void reply(CommandSender cs, Object... formats) {
        reply(true, cs, formats);
    }

    public void reply(boolean flatten, CommandSender cs, Object... formats) {
        String text = build(flatten, flatten(new Object[]{formats, cs}).toArray());
        if (text.contains("[noprefix]"))
            text = text.replace("[noprefix]", "");
        else text = PREFIX.getValue() + text;
        Chat.sendUnprefixed(cs, text);
    }

    private static final Set<Message> MESSAGES = new HashSet<>();
    private static final Map<Message, String> MAPPINGS = new HashMap<>();

    public static void load() {
        for (Field field : Message.class.getDeclaredFields()) {
            if (Message.class.isAssignableFrom(field.getType())) {
                try {
                    Message message = (Message) field.get(null);
                    MESSAGES.add(message);
                    MessageMapping mapping = field.getAnnotation(MessageMapping.class);
                    if (mapping != null)
                        MAPPINGS.put(message, mapping.value());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Map<Message, String> getMappings() {
        return Collections.unmodifiableMap(MAPPINGS);
    }

    @Override public String toString() {
        return create();
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @NotNull
    @Override public Iterator<Message> iterator() {
        return all();
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @NotNull
    public static Iterator<Message> all() {
        return MESSAGES.iterator();
    }
}
