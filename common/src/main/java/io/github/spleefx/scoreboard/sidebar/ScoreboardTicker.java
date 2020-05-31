package io.github.spleefx.scoreboard.sidebar;

import io.github.spleefx.scoreboard.ScoreboardProvider;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class ScoreboardTicker {

    private ScoreboardProvider provider;
    private Map<UUID, SidebarBoard> boards;
    private ScoreboardThread thread;
    private long ticks = 2;

    public ScoreboardTicker() {
        provider = new ScoreboardProvider();
        boards = new ConcurrentHashMap<>();

        setup();
    }

    public void setup() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            SidebarCreateEvent createEvent = new SidebarCreateEvent(player);

            Bukkit.getPluginManager().callEvent(createEvent);
            if (createEvent.isCancelled()) return;

            getBoards().put(player.getUniqueId(), new SidebarBoard(player, this));
        }
        thread = new ScoreboardThread(this);
    }

}
