package io.github.spleefx.spectate;

import io.github.spleefx.SpleefX;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.compatibility.chat.ChatComponent;
import io.github.spleefx.compatibility.chat.ChatEvents.ClickEvent;
import io.github.spleefx.compatibility.chat.ChatEvents.HoverEvent;
import io.github.spleefx.compatibility.chat.ComponentJSON;
import io.github.spleefx.util.game.Chat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class JoinWarningListener implements Listener {

    private final Set<UUID> warned = Collections.synchronizedSet(new HashSet<>());

    private static final ChatComponent WARNING = new ChatComponent().setText(("&b&lNote: &7Spectating is still in &e&lBETA&7. While it should work fine, it is recommended that you try it on a local server in case of any unreported bugs. Should any bugs arise, report them over on").replace(" ", " &7"), true);

    private static final ChatComponent DISCORD = new ChatComponent().setText("&9&lDISCORD&7.", false)
            .setClickAction(ClickEvent.OPEN_URL, "https://discord.gg/uwf72ZN")
            .setHoverAction(HoverEvent.SHOW_TEXT, "Click to prompt the Discord server");

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().isOp()) return;
        if (!SpleefX.getSpectatorSettings().isSendWarningOnStart()) return;
        if (warned.add(event.getPlayer().getUniqueId())) {
            ComponentJSON jsonMessage = new ComponentJSON();
            jsonMessage.append(WARNING).space().append(DISCORD);
            CompatibilityHandler.getProtocol().send(jsonMessage, event.getPlayer());
            Chat.plugin(event.getPlayer(), "&cYou have been warned!");
            jsonMessage.clear().append(new ChatComponent().setText("&e&lClick here to disable this warning message".replace(" ", "&e&l "), true)
                    .setClickAction(ClickEvent.RUN_COMMAND, "/spleefx debug disablespectatingwarningmessage")
                    .setHoverAction(HoverEvent.SHOW_TEXT, "Click to disable."));
            CompatibilityHandler.getProtocol().send(jsonMessage, event.getPlayer());
        }
    }
}