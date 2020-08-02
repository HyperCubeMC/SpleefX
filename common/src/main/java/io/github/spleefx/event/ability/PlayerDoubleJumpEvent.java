/*
 * * Copyright 2020 github.com/ReflxctionDev
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
package io.github.spleefx.event.ability;

import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.event.arena.ArenaEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Fired when a player double-jumps in an arena.
 */
public class PlayerDoubleJumpEvent extends ArenaEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * The player that just jumped
     */
    private final Player player;

    /**
     * The cancelled state of this arena
     */
    private boolean cancelled = false;

    /**
     * The amount double-jumps this player has left
     */
    private final int doubleJumpsLeft;

    public PlayerDoubleJumpEvent(Player player, int doubleJumpsLeft, GameArena arena) {
        super(arena);
        this.doubleJumpsLeft = doubleJumpsLeft;
        this.player = player;
    }

    /**
     * Returns the player that just double jumped
     *
     * @return The player that double jumped
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the amount of double-jumps left for this player
     *
     * @return The amount of double-jumps left for this player.
     */
    public int getDoubleJumpsLeft() {
        return doubleJumpsLeft;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
