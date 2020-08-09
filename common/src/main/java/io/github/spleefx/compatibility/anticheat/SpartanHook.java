package io.github.spleefx.compatibility.anticheat;

import io.github.spleefx.SpleefX;
import io.github.spleefx.extension.ability.DoubleJumpHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import me.vagdedes.spartan.api.PlayerViolationEvent;
import me.vagdedes.spartan.system.Enums.HackType;

public class SpartanHook implements Listener {
    private final SpleefX plugin;

    public SpartanHook(SpleefX plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("Spartan hook enabled!");
    }

    @EventHandler
    public void onViolation(PlayerViolationEvent event) {
        if (event.getHackType() == HackType.IrregularMovements && DoubleJumpHandler.isPlayerDoubleJumping(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
