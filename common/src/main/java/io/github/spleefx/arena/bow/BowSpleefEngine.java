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
package io.github.spleefx.arena.bow;

import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.api.BaseArenaEngine;
import io.github.spleefx.extension.ability.GameAbility;
import io.github.spleefx.team.GameTeam;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.function.Supplier;

import static io.github.spleefx.extension.standard.bowspleef.BowSpleefExtension.EXTENSION;

/**
 * A customized engine for bow spleef to add custom abilities
 */
public class BowSpleefEngine extends BaseArenaEngine<BowSpleefArena> {

    public static final String ARROW_METADATA = "spleefx.bowspleef.projectile";

    /**
     * Creates an engine for the specified arena
     *
     * @param arena Arena to create for
     */
    public BowSpleefEngine(BowSpleefArena arena) {
        super(arena);
    }

    /**
     * Joins the player to the arena
     *
     * @param p Player to join
     */
    @Override
    public boolean join(ArenaPlayer p, GameTeam team) {
        if (super.join(p, team)) {
            abilityCount.get(p.getPlayer().getUniqueId()).put(GameAbility.TRIPLE_ARROWS, EXTENSION.getTripleArrows().getDefaultAmount());
            return true;
        }
        return false;
    }

    /**
     * Returns the scoreboard placeholders map
     *
     * @param player Player to retrieve necessary information from
     * @return The placeholders map
     */
    @Override public Map<String, Supplier<String>> getScoreboardMap(Player player) {
        Map<String, Supplier<String>> map = super.getScoreboardMap(player);
        map.put("{triple_arrows}", () -> Integer.toString(abilityCount.get(player.getUniqueId()).getOrDefault(GameAbility.TRIPLE_ARROWS, 0)));
        return map;
    }
}
