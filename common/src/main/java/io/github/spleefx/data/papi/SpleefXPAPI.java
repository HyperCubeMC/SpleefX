/*
 * * Copyright 2019 github.com/ReflxctionDev
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
package io.github.spleefx.data.papi;

import io.github.spleefx.SpleefX;
import io.github.spleefx.data.GameStats;
import io.github.spleefx.data.LeaderboardTopper;
import io.github.spleefx.data.PlayerStatistic;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.util.code.Printable;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.plugin.PluginSettings;
import lombok.AllArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * PlaceholderAPI expansion for SpleefX
 */
@AllArgsConstructor
public class SpleefXPAPI extends PlaceholderExpansion {

    private static final List<String> INTS = new ArrayList<>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));

    /**
     * The number formatter
     */
    private static final NumberFormat FORMAT = NumberFormat.getInstance(Locale.US);

    /**
     * The SpleefX plugin
     */
    private SpleefX plugin;

    @Override public String getIdentifier() {
        return plugin.getDescription().getName().toLowerCase();
    }

    @Override public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override public boolean persist() {
        return true;
    }

    @Override public boolean canRegister() {
        return true;
    }

    /**
     * called when a placeholder value is requested from this hook
     *
     * @param player     {@link OfflinePlayer} to request the placeholder value for, null if not needed for a
     *                   player
     * @param identifier String passed to the hook to determine what value to return
     * @return value for the requested player and params
     */
    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        if (INTS.stream().anyMatch(identifier::contains)) {
            String[] requested = identifier.split(":");
            String[] split = requested[0].split("_");
            int pos = Integer.parseInt(split[split.length - 1]);
            GameExtension extension = ExtensionsManager.getByKey(requested[1]);
            String request = requested[2];
            PlayerStatistic stat = PlayerStatistic.from(requested[0].substring(0, requested[0].lastIndexOf("_")));
            LeaderboardTopper topper;
            List<LeaderboardTopper> toppers = plugin.getDataProvider().getTopPlayers(stat, extension);
            try {
                topper = toppers.get(pos - 1);
            } catch (IndexOutOfBoundsException e) {
                topper = toppers.get(toppers.size() - 1);
            }
            Printable.print("Found in position " + pos + " the topper " + topper);
            if (topper != null)
                Printable.print("With UUID: " + topper.getPlayer().getUniqueId() + " (name: " + topper.getPlayer().getName() + ")");
            else return "Invalid player";
            switch (request) {
                case "name":
                    return topper.getPlayer().getName();
                case "pos":
                    return format(pos);
                case "stat":
                case "count":
                case "number":
                case "score":
                case "statistic":
                    return format(topper.getCount());
                case "format": {
                    String format = PluginSettings.LEADERBOARDS_FORMAT.get();
                    return Chat.colorize(format)
                            .replace("{player}", topper.getPlayer().getName())
                            .replace("{pos}", format(pos))
                            .replace("{score}", format(topper.getCount()));
                }
                default:
                    return "Invalid request: " + request;
            }
        }
        if (player == null) return format(0);
        GameStats stats = plugin.getDataProvider().getStatistics(player);
        switch (identifier.toLowerCase()) {
            case "games_played":
                return format(stats.get(PlayerStatistic.GAMES_PLAYED, null));
            case "wins":
                return format(stats.get(PlayerStatistic.WINS, null));
            case "losses":
                return format(stats.get(PlayerStatistic.LOSSES, null));
            case "draws":
                return format(stats.get(PlayerStatistic.DRAWS, null));
            case "blocks_mined":
                return format(stats.get(PlayerStatistic.BLOCKS_MINED, null));
            default:
                String[] split = identifier.split("_");
                String key = split[split.length - 1];
                GameExtension mode = ExtensionsManager.getByKey(key);
                switch (identifier.substring(0, identifier.indexOf(split[split.length - 1]) - 1).toLowerCase()) {
                    case "games_played":
                        return format(stats.get(PlayerStatistic.GAMES_PLAYED, mode));
                    case "wins":
                        return format(stats.get(PlayerStatistic.WINS, mode));
                    case "losses":
                        return format(stats.get(PlayerStatistic.LOSSES, mode));
                    case "draws":
                        return format(stats.get(PlayerStatistic.DRAWS, mode));
                    case "blocks_mined":
                        return format(stats.get(PlayerStatistic.BLOCKS_MINED, mode));
                    default:
                        return format(0);
                }
        }
    }

    /**
     * Formats the specified number with commas
     *
     * @param number Number to format
     * @return The formatted string
     */
    private static String format(int number) {
        return FORMAT.format(number);
    }

}
