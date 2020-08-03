package io.github.spleefx.scoreboard.sidebar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;

import java.util.Collections;
import java.util.List;

public class ScoreboardThread extends Thread {

    private ScoreboardTicker ticker;

    ScoreboardThread(ScoreboardTicker ticker) {
        this.ticker = ticker;
        start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                tick();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            try {
                sleep(ticker.getTicks() * 50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            SidebarBoard board = ticker.getBoards().get(player.getUniqueId());

            if (board == null) continue;

            Objective objective = board.getObjective();
            String title = ticker.getProvider().getTitle(player);
            if (title == null) {
                ticker.getBoards().remove(player.getUniqueId());
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                return;
            }
            if (!objective.getDisplayName().equals(title)) {
                objective.setDisplayName(title);
            }
            List<String> newLines = ticker.getProvider().getLines(player);
            if (newLines == null || newLines.isEmpty()) {
                board.getEntries().forEach(ScoreboardEntry::remove);
                board.getEntries().clear();
            } else {
                Collections.reverse(newLines);

                if (board.getEntries().size() > newLines.size()) {
                    for (int i = newLines.size(); i < board.getEntries().size(); i++) {
                        ScoreboardEntry entry = board.getEntryAtPosition(i);

                        if (entry != null) {
                            entry.remove();
                        }
                    }
                }

                int cache = 1;
                for (int i = 0; i < newLines.size(); i++) {
                    ScoreboardEntry entry = board.getEntryAtPosition(i);

                    String line = ChatColor.translateAlternateColorCodes('&', newLines.get(i));
                    if (entry == null) {
                        entry = new ScoreboardEntry(board, line);
                    }
                    entry.setText(line);
                    entry.setup();
                    entry.send(cache++);
                }
            }
        }
    }
}
