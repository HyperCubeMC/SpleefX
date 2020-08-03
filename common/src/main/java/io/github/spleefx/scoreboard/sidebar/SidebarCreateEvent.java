package io.github.spleefx.scoreboard.sidebar;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class SidebarCreateEvent extends Event implements Cancellable {

    @Getter public static HandlerList handlerList = new HandlerList();

    private Player player;
    private boolean cancelled = false;

    public SidebarCreateEvent(Player player) {
        this.player = player;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }

    @NotNull @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
