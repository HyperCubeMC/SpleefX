package io.github.spleefx.scoreboard.sidebar;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class SidebarBoard {

    private final List<ScoreboardEntry> entries = new ArrayList<>();
    private final List<String> identifiers = new ArrayList<>();
    private Scoreboard scoreboard;
    private Objective objective;
    private UUID uuid;

    private ScoreboardTicker scoreboardTicker;

    public SidebarBoard(Player player, ScoreboardTicker scoreboardTicker) {
        this.scoreboardTicker = scoreboardTicker;
        setup(player);
        uuid = player.getUniqueId();
    }

    private void setup(Player player) {
        // Register new scoreboard if needed
        if (player.getScoreboard() == Bukkit.getScoreboardManager().getMainScoreboard()) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        } else {
            scoreboard = player.getScoreboard();
        }

        // Setup sidebar objective
        objective = scoreboard.registerNewObjective("Default", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        getScoreboardTicker().getProvider().getTitle(player).thenAcceptSync((name) -> {
            objective.setDisplayName(name);
            player.setScoreboard(scoreboard);
        });
        // Update scoreboard

    }

    public ScoreboardEntry getEntryAtPosition(int pos) {
        if (pos >= entries.size()) {
            return null;
        } else {
            return entries.get(pos);
        }
    }

    public String getUniqueIdentifier() {
        String identifier = getRandomChatColor() + ChatColor.WHITE;

        while (identifiers.contains(identifier)) {
            identifier = identifier + getRandomChatColor() + ChatColor.WHITE;
        }

        if (identifier.length() > 16) {
            return getUniqueIdentifier();
        }

        identifiers.add(identifier);

        return identifier;
    }

    private static String getRandomChatColor() {
        return ChatColor.values()[ThreadLocalRandom.current().nextInt(ChatColor.values().length)].toString();
    }

}
