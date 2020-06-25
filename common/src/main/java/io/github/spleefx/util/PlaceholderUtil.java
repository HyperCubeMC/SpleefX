package io.github.spleefx.util;

import io.github.spleefx.arena.api.BaseArenaEngine;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.plugin.PluginSettings;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.Map;

import static io.github.spleefx.message.MessageKey.formatTime;
import static io.github.spleefx.util.PlaceholderUtil.PlaceholderFiller.p;

public class PlaceholderUtil {

    private static final boolean PAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    /**
     * Placeholder filler for offline players
     */
    public static final PlaceholderFiller<OfflinePlayer> OFFLINE_PLAYER = (player, b) -> {
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
    public static final PlaceholderFiller<Player> PLAYER = (player, b) -> {
        p(b, "player_displayname", player.getDisplayName());
        p(b, "player_health", (int) player.getHealth());
        if (PAPI) {
            String created = PlaceholderAPI.setPlaceholders(player, b.toString());
            b.clear().append(created);
        }
    };

    /**
     * Placeholder filler for arenas
     */
    public static final PlaceholderFiller<GameArena> ARENA = (arena, b) -> {
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
        }
        if (PAPI)
            builder.clear().append(PlaceholderAPI.setPlaceholders(null, builder.toString()));
        return Chat.colorize(builder.toString());
    }
}
