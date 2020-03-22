/*
 * * Copyright 2019-2020 github.com/ReflxctionDev
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

import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.api.BaseArenaEngine;
import io.github.spleefx.util.game.Chat;
import io.github.thatkawaiisam.assemble.AssembleAdapter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class GameScoreboardAdapter implements AssembleAdapter {

    @Override
    public String getTitle(Player player) {
        try {
            return Chat.colorize(getScoreboardHolder(ArenaPlayer.adapt(player)).getTitle());
        } catch (NullPointerException e) {
            return null;
        }
    }

    @Override
    public List<String> getLines(Player player) {
        ArenaPlayer p = ArenaPlayer.adapt(player);
        BaseArenaEngine<?> engine = (BaseArenaEngine<?>) p.getCurrentArena().getEngine();
        ScoreboardHolder holder = getScoreboardHolder(p);
        if (holder == null)
            throw new RuntimeException("Cannot find a scoreboard section for extension " + p.getCurrentArena().getExtension().getKey() + ". Please add a section to remove this error.");
        return holder.getText()
                .values()
                .stream()
                .map(s -> s.trim().isEmpty() ? "" : holder.replacePlaceholders(p, s, p.getCurrentArena(), engine.getScoreboardMap(player)))
                .collect(Collectors.toList());
    }

    private static ScoreboardHolder getScoreboardHolder(ArenaPlayer p) {
        return p.getCurrentArena().getExtension().getScoreboard().get(p.getCurrentArena().getEngine().getArenaStage());
    }

}