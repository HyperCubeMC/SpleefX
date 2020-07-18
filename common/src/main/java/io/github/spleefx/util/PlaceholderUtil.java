package io.github.spleefx.util;

import io.github.spleefx.arena.api.BaseArenaEngine;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.economy.booster.BoosterFactory;
import io.github.spleefx.economy.booster.BoosterInstance;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.extension.standard.splegg.SpleggUpgrade;
import io.github.spleefx.perk.GamePerk;
import io.github.spleefx.team.GameTeam;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.plugin.PluginSettings;
import lombok.AllArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Map;
import java.util.Objects;

import static io.github.spleefx.util.PlaceholderUtil.PlaceholderFiller.p;

public class PlaceholderUtil {

    public static final boolean PAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    /**
     * Placeholder filler for offline players
     */
    private static final PlaceholderFiller<OfflinePlayer> OFFLINE_PLAYER = (player, b) -> {
        p(b, "player", player.getName() == null ? "NoName" : player.getName());
        p(b, "player_name", player.getName() == null ? "NoName" : player.getName());
        if (PAPI) {
            String created = PlaceholderAPI.setPlaceholders(player, b.toString());
            b.clear().append(created);
        }
    };

    /**
     * Placeholder filler for players
     */
    private static final PlaceholderFiller<Player> PLAYER = (player, b) -> {
        p(b, "player_displayname", player.getDisplayName());
        p(b, "player_health", (int) player.getHealth());
        if (PAPI) {
            String built = b.toString();
            String created = PlaceholderAPI.setPlaceholders(player, built);
            b.clear().append(created);
        }
    };

    /**
     * Placeholder filler for teams
     */
    private static final PlaceholderFiller<GameTeam> TEAM = (team, b) -> {
        p(b, "team", team.getColor().chat());
        p(b, "team_color", team.getColor().getChatColor());
    };

    /**
     * Placeholder filler for locations
     */
    private static final PlaceholderFiller<Location> LOCATION = (loc, b) -> {
        p(b, "x", loc.getX());
        p(b, "y", loc.getY());
        p(b, "z", loc.getZ());
        p(b, "world", Objects.requireNonNull(loc.getWorld(), "location#getWorld() is null!").getName());
    };

    /**
     * Placeholder filler for command parameters
     */
    private static final PlaceholderFiller<String[]> COMMAND_ARGS = (args, b) -> {
        for (int i = 0; i < args.length; i++) {
            p(b, "args-" + (i + 1), args[i]);
        }
    };

    private static final PlaceholderFiller<CommandEntry> COMMAND_ENTRY = (e, b) -> {
        if (e.command != null)
            p(b, "command", e.command);
        if (e.arena != null)
            p(b, "arena", e.arena);
        if (e.player != null)
            p(b, "player", e.player);
    };

    public static final PlaceholderFiller<BetEntry> BET_ENTRY = (bet, b) -> {
        p(b, "arena_bet", bet.bet);
        if (bet.portion != null)
            p(b, "portion", bet.portion);
    };

    private static final PlaceholderFiller<GamePerk> PERK = (perk, b) -> {
        p(b, "perk_key", perk.getKey());
        p(b, "perk_displayname", perk.getDisplayName());
        p(b, "perk_usable_amount", perk.getPurchaseSettings().getGamesUsableFor());
        p(b, "perk_ingame_amount", perk.getPurchaseSettings().getIngameAmount());
        p(b, "perk_price", NUMBER_FORMAT.format(perk.getPurchaseSettings().getPrice()));
    };

    /**
     * Placeholder filler for game extensions
     */
    private static final PlaceholderFiller<GameExtension> EXTENSION = (extension, b) -> {
        p(b, "extension", extension.getDisplayName());
        p(b, "extension_key", extension.getKey());
        p(b, "extension_chat_prefix", extension.getChatPrefix());
        p(b, "extension_displayname", extension.getDisplayName());
        p(b, "extension_name", extension.getDisplayName());
        p(b, "extension_without_colors", ChatColor.stripColor(Chat.colorize(extension.getDisplayName())));
        p(b, "extension_key", extension.getKey());
    };

    /**
     * Placeholder filler for arenas
     */
    private static final PlaceholderFiller<GameArena> ARENA = (arena, b) -> {
        p(b, "arena", arena.getDisplayName());
        p(b, "arena_key", arena.getKey());
        p(b, "arena_displayname", Chat.colorize(arena.getDisplayName()));
        p(b, "arena_playercount", arena.getEngine().getPlayerTeams().size());
        p(b, "countdown", formatTime(((BaseArenaEngine<?>) arena.getEngine()).countdown));
        String c = ((BaseArenaEngine<?>) arena.getEngine()).countdown + "";
        p(b, "countdown_chat", ((Map<String, String>) PluginSettings.TITLE_ON_COUNTDOWN_NUMBERS.get()).getOrDefault(c, c));
        p(b, "arena_time_left", formatTime(((BaseArenaEngine<?>) arena.getEngine()).timeLeft));
        p(b, "arena_minimum", arena.getMinimum());
        p(b, "arena_maximum", arena.getMaximum());
        p(b, "arena_players_per_team", arena.getMembersPerTeam());
        p(b, "arena_stage", arena.getEngine().getArenaStage().getState());
        p(b, "arena_alive", arena.getEngine().getAlive().size());
        EXTENSION.apply(arena.getExtension(), b);
    };

    private static final PlaceholderFiller<SpleggUpgrade> SPLEGG_UPGRADE = (upgrade, b) -> {
        p(b, "upgrade_key", upgrade.getKey());
        p(b, "upgrade_displayname", upgrade.getDisplayName());
        p(b, "upgrade_price", NUMBER_FORMAT.format(upgrade.getPrice()));
        p(b, "upgrade_key", Double.toString(upgrade.getDelay()));
    };

    private static final PlaceholderFiller<Integer> INTEGER = (value, b) -> {
        p(b, "value", value);
        p(b, "value_formatted", NUMBER_FORMAT.format(value));
        p(b, "plural", value != 1 ? "s" : "");
    };

    public static final PlaceholderFiller<BoosterInstance> BOOSTER = (booster, b) -> {
        BoosterFactory type = booster.getType();
        p(b, "booster_limit", Integer.toString(BoosterFactory.ALLOW_MULTIPLE.get()));
        p(b, "booster_type_displayname", type.getDisplayName());
        p(b, "booster_type", type.getDisplayName()); // fallback lol
        p(b, "booster_type_key", type.getKey());
        p(b, "duration", type.getDuration().toString());
        p(b, "booster_multiplier", Double.toString(booster.getMultiplier()));
        p(b, "booster_time_left", Long.toString(booster.getDuration()));
        p(b, "booster_type_duration", type.getDuration().toString());
        p(b, "booster_is_active", booster.isActive() ? "&cActive" : "&aAvailable");
    };

    public static final PlaceholderFiller<ColoredNumberEntry> COLORED_NUMBER = (number, b) -> {
        p(b, "colored_number", number.value);
    };

    @FunctionalInterface
    public interface PlaceholderFiller<T> {

        void apply(T value, StrBuilder builder);

        static void p(StrBuilder builder, String placeholder, Object value) {
            builder.replaceAll("{" + placeholder + "}", value.toString());
        }
    }

    public static String all(String original, Object... formats) {
        StrBuilder builder = new StrBuilder(original);
        for (Object o : formats) {
            if (o instanceof Player) PLAYER.apply((Player) o, builder);
            if (o instanceof OfflinePlayer) OFFLINE_PLAYER.apply((OfflinePlayer) o, builder);
            if (o instanceof GameExtension) EXTENSION.apply((GameExtension) o, builder);
            if (o instanceof GameArena) ARENA.apply((GameArena) o, builder);
            if (o instanceof GameTeam) TEAM.apply((GameTeam) o, builder);
            if (o instanceof String[]) COMMAND_ARGS.apply((String[]) o, builder);
            if (o instanceof SpleggUpgrade) SPLEGG_UPGRADE.apply((SpleggUpgrade) o, builder);
            if (o instanceof Integer) INTEGER.apply((Integer) o, builder);
            if (o instanceof CommandEntry) COMMAND_ENTRY.apply((CommandEntry) o, builder);
            if (o instanceof BetEntry) BET_ENTRY.apply((BetEntry) o, builder);
            if (o instanceof ColoredNumberEntry) COLORED_NUMBER.apply((ColoredNumberEntry) o, builder);
            if (o instanceof Location) LOCATION.apply((Location) o, builder);
            if (o instanceof GamePerk) PERK.apply((GamePerk) o, builder);
            if (o instanceof BoosterInstance) BOOSTER.apply((BoosterInstance) o, builder);
        }
        if (PAPI) {
            String created = PlaceholderAPI.setPlaceholders(null, builder.toString());
            builder.clear().append(created);
        }
        return Chat.colorize(builder.toString());
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

    public static class CommandEntry {

        @Nullable private String command;
        @Nullable private String arena;
        @Nullable private String player;

        public CommandEntry(@Nullable String command) {
            this.command = command;
        }

        public CommandEntry(@Nullable String command, @Nullable String arena) {
            this.command = command;
            this.arena = arena;
        }

        public CommandEntry(@Nullable String command, @Nullable String arena, @Nullable String player) {
            this.command = command;
            this.arena = arena;
            this.player = player;
        }
    }

    @AllArgsConstructor
    public static class BetEntry {

        private int bet;
        @Nullable private String portion;
    }

    @AllArgsConstructor
    public static class ColoredNumberEntry {

        private String value;
    }

}
