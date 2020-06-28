package io.github.spleefx.command.parent.sub;

import io.github.spleefx.command.sub.PluginSubcommand;
import io.github.spleefx.util.game.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class DiscordSubcommand extends PluginSubcommand {

    private static final Permission PERMISSION = new Permission("spleefx.discord", PermissionDefault.TRUE);

    public DiscordSubcommand() {
        super("discord", c -> PERMISSION, "Get the URL of the official Discord support server", c -> "/spleefx discord");
    }

    /**
     * Handles the command logic
     *
     * @param command The bukkit command
     * @param sender  Sender of the command
     * @param args    Command arguments
     * @return {@code false} if it is desired to send the help menu, true if otherwise.
     */
    @Override
    public boolean handle(Command command, CommandSender sender, String[] args) {
        Chat.plugin(sender, "&7Join us on Discord: &bhttps://discord.gg/uwf72ZN");
        return true;
    }
}
