package io.github.spleefx.scoreboard.sidebar;

import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardEntry {

    private final SidebarBoard board;

    @Setter
    private String text, identifier;
    private Team team;

    public ScoreboardEntry(SidebarBoard board, String text) {
        this.board = board;
        this.text = text;
        identifier = this.board.getUniqueIdentifier();

        setup();
    }

    public void setup() {
        final Scoreboard scoreboard = board.getScoreboard();

        if (scoreboard == null) {
            return;
        }


        String teamName = identifier;

        if (teamName.length() > 16) {
            teamName = teamName.substring(0, 16);
        }

        Team team = scoreboard.getTeam(teamName);

        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        if (!team.getEntries().contains(identifier)) {
            team.addEntry(identifier);
        }

        if (!board.getEntries().contains(this)) {
            board.getEntries().add(this);
        }

        this.team = team;
    }

    public void send(int position) {
        if (text.length() > 16) {
            String prefix = text.substring(0, 16);
            String suffix;

            if (prefix.charAt(15) == ChatColor.COLOR_CHAR) {
                prefix = prefix.substring(0, 15);
                suffix = text.substring(15);
            } else if (prefix.charAt(14) == ChatColor.COLOR_CHAR) {
                prefix = prefix.substring(0, 14);
                suffix = text.substring(14);
            } else {
                if (ChatColor.getLastColors(prefix).equalsIgnoreCase(ChatColor.getLastColors(identifier))) {
                    suffix = text.substring(16);
                } else {
                    suffix = ChatColor.getLastColors(prefix) + text.substring(16);
                }
            }

            if (suffix.length() > 16) {
                suffix = suffix.substring(0, 16);
            }

            team.setPrefix(prefix);
            team.setSuffix(suffix);
        } else {
            team.setPrefix(text);
            team.setSuffix("");
        }

        Score score = board.getObjective().getScore(identifier);
        score.setScore(position);
    }

    public void remove() {
        board.getIdentifiers().remove(identifier);
        board.getScoreboard().resetScores(identifier);
    }

}
