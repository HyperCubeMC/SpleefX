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
package io.github.spleefx.team;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a Spleef team that is in a game
 */
@Getter
public class GameTeam {

    /**
     * Represents the color of the team
     */
    private TeamColor color;

    /**
     * Lists for tracking team players
     */
    private List<Player> members, alive;

    public GameTeam(TeamColor color, List<Player> members) {
        this.color = color;
        this.members = members;
        alive = new CopyOnWriteArrayList<>(members);
    }

    public boolean isEliminated() {
        return alive.size() == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameTeam team = (GameTeam) o;
        return color == team.color;
    }
}