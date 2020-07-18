package io.github.spleefx.command.sub.base;

import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.api.ArenaData;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.command.sub.PluginSubcommand;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.util.PlaceholderUtil.CommandEntry;
import io.github.spleefx.util.message.message.Message;
import io.github.spleefx.util.plugin.ArenaSelector;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JoinSubcommand extends PluginSubcommand {

    private static final List<String> HELP = Collections.singletonList(
            "&ejoin &a<arena> &7- &dJoin the specified arena"
    );

    protected Function<Command, GameExtension> extension;

    public JoinSubcommand(Function<Command, GameExtension> extension) {
        super("join",
                (c) -> new Permission("spleefx." + c.getName() + ".join", PermissionDefault.TRUE),
                "Join an arena",
                (c) -> "/" + c.getName() + " join <arena>");
        this.extension = extension;
        super.helpMenu = HELP;
    }

    /**
     * Handles the command input
     *
     * @param sender Command sender
     * @param args   Extra command arguments
     * @return {@code true} if the command succeed, {@code false} if it is desired to send {@link #getHelpMenu()}.
     */
    @Override
    public boolean handle(Command command, CommandSender sender, String[] args) {
        GameExtension extension = this.extension.apply(command);
        if (checkSender(sender)) {
            Message.NOT_PLAYER.reply(sender, extension);
            return true;
        }
        ArenaPlayer player = ArenaPlayer.adapt(((Player) sender));
        if (args.length == 0) {
            GameArena arena = ArenaSelector.pick(extension);
            if (arena == null) {
                Message.NO_AVAILABLE_ARENA.reply(sender, player.getPlayer(), command.getName(), extension);
                return true;
            }
            arena.getEngine().join(player, null);
            return true;
        }
        String arenaKey = args[0];
        GameArena arena = GameArena.getByKey(arenaKey); // Get by key
        if (arena == null) arena = GameArena.getByName(arenaKey = combine(args, 0)); // Get by display name
        if (arena == null) {
            Message.INVALID_ARENA.reply(sender, extension, new CommandEntry(command.getName(), args[1]));
            return true;
        }
        arena.getEngine().join(player, null);
        return true;
    }

    /**
     * Returns a list of tabs for this subcommand.
     *
     * @param args Command arguments. Does <i>NOT</i> channelTo this subcommand.
     * @return A list of all tabs.
     */
    @Override
    public List<String> onTab(CommandSender sender, Command command, String[] args) {
        GameExtension extension = this.extension.apply(command);
        if (args.length == 1) {
            return GameArena.ARENAS.get().values().stream().filter(arena -> extension.equals(arena.getExtension())).map(ArenaData::getKey).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}