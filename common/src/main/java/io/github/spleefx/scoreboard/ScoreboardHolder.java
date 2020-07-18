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
package io.github.spleefx.scoreboard;

import com.google.gson.annotations.Expose;
import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.api.ArenaType;
import io.github.spleefx.arena.api.BaseArenaEngine;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.scoreboard.sidebar.SidebarBoard;
import io.github.spleefx.team.GameTeam;
import io.github.spleefx.util.PlaceholderUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import static io.github.spleefx.SpleefX.getPlugin;

@Getter
public class ScoreboardHolder {

    @Expose
    private boolean enabled = true;

    @Expose
    private String title = "";

    @Expose
    private Map<Integer, String> text = new LinkedHashMap<>();

    public void createScoreboard(ArenaPlayer p) {
        if (!isEnabled()) return;
        getPlugin().getScoreboardTicker().getBoards().put(p.getPlayer().getUniqueId(), new SidebarBoard(p.getPlayer(), getPlugin().getScoreboardTicker()));
    }

    public static String replacePlaceholders(@Nullable ArenaPlayer player, String message, GameArena arena, Map<String, Supplier<String>> placeholders) {
        BaseArenaEngine<? extends GameArena> engine = (BaseArenaEngine<? extends GameArena>) arena.getEngine();

        @Nullable Location location = null;
        if (player != null) location = player.getPlayer().getLocation();
        for (Entry<String, Supplier<String>> placeholder : placeholders.entrySet())
            message = message.replace(placeholder.getKey(), placeholder.getValue().get());
        List<Object> formats = new ArrayList<>();
        if (player != null)
            formats.add(player.getPlayer());
        formats.add(arena);
        if (location != null)
            formats.add(location);

        if (arena.getArenaType() == ArenaType.TEAMS && player != null) {
            GameTeam team = engine.getPlayerTeams().get(player);
            if (team != null)
                formats.add(team);
        }
        return PlaceholderUtil.all(message, formats.toArray());
    }

}