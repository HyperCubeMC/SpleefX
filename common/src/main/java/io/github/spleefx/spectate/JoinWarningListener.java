package io.github.spleefx.spectate;

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

    private Set<UUID> warned = Collections.synchronizedSet(new HashSet<>());

    private static final ChatComponent WARNING = new ChatComponent().setText(("&b&lNote: &7While it has been heavily tested, ironed and polished, spectating is still in beta and may still contain certain bugs" +
            " that may have consequences (such as inventory loss, etc). It's &c&lHIGHLY " +
            "&7recommended that you test this version on a local server before using it in production. Should any bugs arise, downgrade to the before-lastest and send us a report over on").replace(" ", " &7"), true);

    private static final ChatComponent DISCORD = new ChatComponent().setText("&9&lDISCORD", false)
            .setClickAction(ClickEvent.OPEN_URL, "https://discord.gg/uwf72ZN")
            .setHoverAction(HoverEvent.SHOW_TEXT, "Click to prompt the Discord server");

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().isOp()) return;
        if (warned.add(event.getPlayer().getUniqueId())) {
            ComponentJSON jsonMessage = new ComponentJSON();
            jsonMessage.append(WARNING).space().append(DISCORD);
            CompatibilityHandler.getProtocol().send(jsonMessage, event.getPlayer());
            Chat.plugin(event.getPlayer(), "&cYou have been warned!");
        }
    }
}