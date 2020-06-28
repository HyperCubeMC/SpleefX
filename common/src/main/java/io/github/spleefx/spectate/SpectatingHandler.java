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
package io.github.spleefx.spectate;

import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.ArenaPlayer.ArenaPlayerState;
import io.github.spleefx.compatibility.CompatibilityHandler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static io.github.spleefx.SpleefX.getSpectatorSettings;

public class SpectatingHandler {

    private static final PotionEffect INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 2);
    private static final ItemStack[] ARMOR = new ItemStack[4];

    public void putSpectationMode(Player sp) {
        ArenaPlayer player = ArenaPlayer.adapt(sp);
        player.setSpectating(true);
        player.setState(ArenaPlayerState.SPECTATING);
        sp.setAllowFlight(true);
        sp.addPotionEffect(INVISIBILITY);
        sp.setFlying(true);
        sp.setGameMode(GameMode.SURVIVAL);
        sp.getInventory().clear();
        sp.getInventory().setArmorContents(ARMOR);
        getSpectatorSettings().getSpectateItem().give(sp);
        getSpectatorSettings().getExitSpectatingItem().give(sp);
        sp.setHealth(20);
        sp.setFoodLevel(20);
        sp.setAllowFlight(true);
        CompatibilityHandler.either(() -> {
            sp.setCollidable(false);
            return false;
        }, () -> false);
        sp.getActivePotionEffects().forEach(e -> sp.removePotionEffect(e.getType()));
        sp.addPotionEffects(getSpectatorSettings().getGivePotionEffects());
        Bukkit.getOnlinePlayers().forEach(p -> CompatibilityHandler.getProtocol().hidePlayer(p, sp));
    }

    public void disableSpectationMode(Player sp) {
        ArenaPlayer player = ArenaPlayer.adapt(sp);
        if (!player.isSpectating()) return;
        player.setSpectating(false);
        player.setState(ArenaPlayerState.NOT_INGAME);
        sp.removePotionEffect(INVISIBILITY.getType());
        Bukkit.getOnlinePlayers().forEach(p -> CompatibilityHandler.getProtocol().showPlayer(p, sp));
    }
}
