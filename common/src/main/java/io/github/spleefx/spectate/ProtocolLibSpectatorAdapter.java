package io.github.spleefx.spectate;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;

/**
 * Used as a workaround for a bug in 1.8 where {@link TeleportCause#SPECTATE} is not fired.
 */
public class ProtocolLibSpectatorAdapter extends PacketAdapter {

    public ProtocolLibSpectatorAdapter(Plugin plugin) {
        super(plugin, ListenerPriority.NORMAL, Server.CAMERA);
        ProtocolLibrary.getProtocolManager().addPacketListener(this);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.getPacketType() == Server.CAMERA) {
            if (event.getPlayer().getSpectatorTarget() == null)
                Bukkit.getPluginManager().callEvent(new PlayerExitSpectateEvent(event.getPlayer()));
        }
    }
}