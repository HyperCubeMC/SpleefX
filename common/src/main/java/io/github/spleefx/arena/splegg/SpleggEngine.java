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
package io.github.spleefx.arena.splegg;

import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.api.BaseArenaEngine;
import io.github.spleefx.extension.standard.splegg.SpleggUpgrade;
import io.github.spleefx.team.GameTeam;

import java.util.Optional;

import static io.github.spleefx.extension.standard.splegg.SpleggExtension.EXTENSION;

/**
 * A custom engine for splegg arenas
 */
public class SpleggEngine extends BaseArenaEngine<SpleggArena> {

    public SpleggEngine(SpleggArena arena) {
        super(arena);
    }

    /**
     * Prepares the player to the game, by teleporting, adding items, etc
     *
     * @param player Player to prepare
     * @param team   The player's team
     */
    @Override
    public void prepareForGame(ArenaPlayer player, GameTeam team) {
        super.prepareForGame(player, team);
        if (EXTENSION.isUpgradeSystemEnabled()) {
            Optional<SpleggUpgrade> defUpgrade = EXTENSION.getUpgrades().values().stream().filter(SpleggUpgrade::isDefault).findAny();
            if (!defUpgrade.isPresent()) return;
            SpleggUpgrade u = EXTENSION.getUpgrades().get(player.getStats().getSelectedSpleggUpgrade());
            player.getPlayer().getInventory().setItem(u.getGameItem().getSlot(), u.getGameItem().createItem(player.getPlayer(), u));
        } else
            player.getPlayer().getInventory().setItem(EXTENSION.getProjectileItemSlot(), EXTENSION.getProjectileItem().factory().create());
    }

    /**
     * Starts the game
     */
    @Override public void start() {
        super.start();
        SpleggListener.delayHandler.start();
    }
}
