package io.github.spleefx.config;

import io.github.spleefx.SpleefX;
import io.github.spleefx.extension.ExtensionTitle;
import io.github.spleefx.util.plugin.Protocol;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.List;
import java.util.Map;

import static io.github.spleefx.config.ValueFactory.*;

/**
 * A wrapper for SpleefX's 'config.yml'.
 */
public interface SpleefXConfig {

    /**
     * The update interval in ticks for arenas
     */
    ConfigOption<Integer> ARENA_UPDATE_INTERVAL = integerKey("Arena.LoopUpdateInterval", 20);

    /**
     * Whether should damage between teams be cancelled or not.
     */
    ConfigOption<Boolean> ARENA_CANCEL_TEAM_DAMAGE = booleanKey("Arena.CancelTeamDamage", true);

    /**
     * Whether do arenas require an empty inventory before joining
     */
    ConfigOption<Boolean> ARENA_REQUIRE_EMPTY_INV = booleanKey("Arena.RequireEmptyInventoryBeforeJoining", false);

    /**
     * The melting radius
     */
    ConfigOption<Integer> ARENA_MELTING_RADIUS = integerKey("Arena.Melting.Radius", 5);

    /**
     * The melting interval
     */
    ConfigOption<Integer> ARENA_MELTING_INTERVAL = integerKey("Arena.Melting.Interval", 100);

    ConfigOption<Boolean> ARENA_MELTING_IGNORE_X = booleanKey("Arena.Melting.IgnoreX", false);
    ConfigOption<Boolean> ARENA_MELTING_IGNORE_Y = booleanKey("Arena.Melting.IgnoreY", true);
    ConfigOption<Boolean> ARENA_MELTING_IGNORE_Z = booleanKey("Arena.Melting.IgnoreZ", false);

    /**
     * A list of all materials that can be melted
     */
    ConfigOption<List<Material>> ARENA_MELTABLE_BLOCKS = materialList("Arena.Melting.MeltableBlocks");

    /**
     * Whether should arenas regenerate when countdown starts
     */
    ConfigOption<Boolean> ARENA_REGENERATE_BEFORE_COUNTDOWN = booleanKey("Arena.RegenerateBeforeGameStarts", true);

    /**
     * The interval of updating signs
     */
    ConfigOption<Integer> SIGN_UPDATE_INTERVAL = integerKey("Arena.SignUpdateInterval", 40);

    /**
     * The update interval of scoreboards
     */
    ConfigOption<Integer> SCOREBOARD_UPDATE_INTERVAL = integerKey("Arena.ScoreboardUpdateInterval", 10);

    /**
     * Whether should countdown be displayed on exp bar
     */
    ConfigOption<Boolean> DISPLAY_COUNTDOWN_ON_EXP_BAR = booleanKey("Countdown.DisplayOnExpBar", true);

    /**
     * The countdown on enough players
     */
    ConfigOption<Integer> COUNTDOWN_ON_ENOUGH_PLAYERS = integerKey("Countdown.OnEnoughPlayers", 20);

    /**
     * Whether should a sound be played on each countdown broadcast
     */
    ConfigOption<Boolean> PLAY_SOUND_ON_EACH_BROADCAST_ENABLED = booleanKey("Countdown.PlaySoundOnEachBroadcast.Enabled", true);

    /**
     * Sound to play on each broadcast
     */
    ConfigOption<Sound> PLAY_SOUND_ON_EACH_BROADCAST_SOUND = enumKey("Countdown.PlaySoundOnEachBroadcast.Sound", Protocol.isNewerThan(9) ? Sound.valueOf("BLOCK_LEVER_CLICK") : Sound.valueOf("CLICK"));

    /**
     * When to play the sounds
     */
    ConfigOption<List<Integer>> PLAY_SOUND_ON_EACH_BROADCAST_WHEN = intList("Countdown.PlaySoundOnEachBroadcast.PlayWhenCountdownIs");

    /**
     * Title to display on each countdown
     */
    ConfigOption<ExtensionTitle> TITLE_ON_COUNTDOWN = option((config, path, def) -> {
        boolean enabled = config.getBoolean("TitleOnCountdown.Enabled", true);
        int fadeIn = config.getInt("TitleOnCountdown.FadeIn", 5);
        int display = config.getInt("TitleOnCountdown.Display", 10);
        int fadeOut = config.getInt("TitleOnCountdown.FadeOut", 5);
        String subtitle = config.getString("TitleOnCountdown.Subtitle");
        return new ExtensionTitle(enabled, null, subtitle, fadeIn, display, fadeOut);
    });

    ConfigOption<Map<Integer, String>> TITLE_ON_COUNTDOWN_NUMBERS = integerMap("TitleOnCountdown.NumbersToDisplay");

    /**
     * Numbers to warn on in time out
     */
    ConfigOption<Map<Integer, String>> TIME_OUT_WARN = integerMap("TimeOut.NumbersToWarnOn");

    /**
     * The "all modes" value in the stats GUI
     */
    ConfigOption<String> ALL_MODES_NAME = stringKey("PlayerGameStatistics.AllModesName", "All Modes");

    /**
     * Whether should SpleefX register its Vault hooks or not
     */
    ConfigOption<Boolean> ECO_HOOK_INTO_VAULT = notReloadable(booleanKey("Economy.HookIntoVault", true));

    /**
     * Whether should the economy be completely dependant on Vault.
     */
    ConfigOption<Boolean> ECO_USE_VAULT = booleanKey("Economy.GetFromVault");

    /**
     * Whether are leaderboards enabled
     */
    ConfigOption<Boolean> LEADERBOARDS = notReloadable(booleanKey("Leaderboards.Enabled"));

    /**
     * The leaderboard format
     */
    ConfigOption<String> LEADERBOARDS_FORMAT = stringKey("Leaderboards.Format", "&d#{pos} &e{player} &7- &b{score}");

    /**
     * Whether does vault exist or not
     */
    boolean VAULT_EXISTS = Bukkit.getPluginManager().isPluginEnabled("Vault");

    /**
     * A list of all options in this configuration class
     */
    List<ConfigOption<?>> OPTIONS = ConfigOption.locateSettings(SpleefXConfig.class);

    /**
     * Loads all options in this class
     *
     * @param initial Whether is this the first time to load or not. Used as a
     *                tiny firewall to protect non-{@link ConfigOption#reloadable} keys.
     */
    static void load(boolean initial) {
        ConfigOption.load(OPTIONS, SpleefX.getPlugin().getConfig(), initial);
    }

}
