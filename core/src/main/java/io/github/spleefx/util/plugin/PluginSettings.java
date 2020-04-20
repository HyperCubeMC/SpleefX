package io.github.spleefx.util.plugin;

import io.github.spleefx.SpleefX;
import io.github.spleefx.data.DataProvider.PlayerStoringStrategy;
import io.github.spleefx.data.DataProvider.StorageType;
import org.bukkit.Sound;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Contains all plugin settings
 */
@SuppressWarnings("unchecked") // Lots of casts for generics
public enum PluginSettings {

    ARENA_UPDATE_INTERVAL("Arena.LoopUpdateInterval", 20),
    ARENA_CANCEL_TEAM_DAMAGE("Arena.CancelTeamDamage", true),
    ARENA_REQUIRE_EMPTY_INV("Arena.RequireEmptyInventoryBeforeJoining", false),
    ARENA_MELTING_RADIUS("Arena.Melting.Radius", 5),
    ARENA_MELTING_INTERVAL("Arena.Melting.Interval", 100),
    ARENA_MELTING_IGNORE_X("Arena.Melting.IgnoreX", false),
    ARENA_MELTING_IGNORE_Y("Arena.Melting.IgnoreY", true),
    ARENA_MELTING_IGNORE_Z("Arena.Melting.IgnoreZ", false),
    ARENA_MELTING_BLOCKS("Arena.Melting.MeltableBlocks", Collections.singletonList("SNOW_BLOCK")),
    ARENA_REGENERATE_BEFORE_COUNTDOWN("Arena.RegenerateBeforeGameStarts", true),
    SIGN_UPDATE_INTERVAL("Arena.SignUpdateInterval", 40),
    SCOREBOARD_UPDATE_INTERVAL("Arena.ScoreboardUpdateInterval", 10),

    DISPLAY_COUNTDOWN_ON_EXP_BAR("Countdown.DisplayOnExpBar", true),
    COUNTDOWN_ON_ENOUGH_PLAYERS("Countdown.OnEnoughPlayers", 20),

    PLAY_SOUND_ON_EACH_BROADCAST_ENABLED("Countdown.PlaySoundOnEachBroadcast.Enabled", true),
    PLAY_SOUND_ON_EACH_BROADCAST_SOUND("Countdown.PlaySoundOnEachBroadcast.Sound", Protocol.isNewerThan(9) ? Sound.valueOf("BLOCK_LEVER_CLICK") : Sound.valueOf("CLICK")),
    PLAY_SOUND_ON_EACH_BROADCAST_WHEN("Countdown.PlaySoundOnEachBroadcast.PlayWhenCountdownIs", Arrays.asList(45, 30, 20, 15, 10, 5, 4, 3, 2, 1)),

    TITLE_ON_COUNTDOWN_ENABLED("TitleOnCountdown.Enabled", true),
    TITLE_ON_COUNTDOWN_FADE_IN("TitleOnCountdown.FadeIn", 5),
    TITLE_ON_COUNTDOWN_DISPLAY("TitleOnCountdown.Display", 10),
    TITLE_ON_COUNTDOWN_FADE_OUT("TitleOnCountdown.FadeOut", 5),
    TITLE_ON_COUNTDOWN_SUBTITLE("TitleOnCountdown.Subtitle", ""),
    TITLE_ON_COUNTDOWN_NUMBERS("TitleOnCountdown.NumbersToDisplay", Collections.emptyMap()),

    TIME_OUT_WARN("TimeOut.NumbersToWarnOn", Collections.emptyMap()),

    ALL_MODES_NAME("PlayerGameStatistics.AllModesName", "All Modes"),
    STATISTICS_STORAGE_TYPE("PlayerGameStatistics.StorageType", StorageType.FLAT_FILE),
    STATISTICS_DIRECTORY("PlayerGameStatistics.Directory", "player-data"),
    STATISTICS_STORE_PLAYERS_BY("PlayerGameStatistics.StorePlayersBy", PlayerStoringStrategy.UUID),
    UNITED_FILE_NAME("PlayerGameStatistics.UnitedFile.FileName", "player-data.json"),
    SQLITE_FILE_NAME("PlayerGameStatistics.SQLite.FileName", "player-data.db"),
    ECO_HOOK_INTO_VAULT("Economy.HookIntoVault", true),
    ECO_USE_VAULT("Economy.GetFromVault", false),

    LEADERBOARDS("Leaderboards.Enabled", false),
    LEADERBOARDS_FORMAT("Leaderboards.Format", "&d#{pos} &e{player} &7- &b{score}");

    public static final PluginSettings[] values = values();

    /**
     * The config path to the variable
     */
    private final String path;

    /**
     * The default value
     */
    private final Object defaultValue;

    /**
     * Represents the value
     */
    private Object value;

    /**
     * Creates a new setting
     *
     * @param path         Path to the field
     * @param defaultValue Default value
     */
    PluginSettings(String path, Object defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
        value = request();
    }

    /**
     * Returns the field from the cache
     *
     * @param <R> Field type
     * @return The field value
     */
    public <R> R get() {
        return value == null ? request() : (R) value;
    }

    /**
     * Returns the field directly from config
     *
     * @param <R> Field type
     * @return The field value
     */
    public <R> R request() {
        if (defaultValue instanceof Map)
            return (R) (value = requestMap());
        if (defaultValue instanceof Enum)
            return (R) (value = requestEnum());
        return (R) (value = SpleefX.getPlugin().getConfig().get(path, defaultValue));
    }

    /**
     * Returns a {@link Map} derived from a section
     *
     * @param <V> Value type
     * @return The map
     */
    private <V> Map<String, V> requestMap() {
        return getMap(SpleefX.getPlugin().getConfig(), path);
    }

    /**
     * Returns an enumeration of this value
     *
     * @param <E> Enum type
     * @return The enumeration value
     */
    private <E extends Enum<E>> E requestEnum() {
        E e = Enum.valueOf(((Enum<E>) defaultValue).getDeclaringClass(), SpleefX.getPlugin().getConfig().getString(path, ((Enum<E>) defaultValue).name()).toUpperCase());
        if (e == null) return (E) defaultValue;
        return e;
    }

    /**
     * Returns a map which contains all elements inside it in order. Sub-maps are added
     * recursively.
     *
     * @param c    Configuration to get from
     * @param path Path of the map
     * @param <V>  Map value type
     * @return The map
     */
    private static <V> Map<String, V> getMap(FileConfiguration c, String path) {
        Map<String, V> map = new LinkedHashMap<>();
        for (String k : c.getConfigurationSection(path).getKeys(false)) {
            Object o = c.get(path + "." + k);
            if (o instanceof MemorySection) {
                map.put(k, (V) getMap(c, path + "." + k));
                continue;
            }
            map.put(k, (V) o);
        }
        return map;
    }

    /**
     * Invoked in order to load the class
     */
    public static void load() {
    }
}