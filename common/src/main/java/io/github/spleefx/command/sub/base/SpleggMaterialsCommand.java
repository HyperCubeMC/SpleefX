package io.github.spleefx.command.sub.base;

import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.arena.splegg.SpleggArena;
import io.github.spleefx.command.sub.CommandException;
import io.github.spleefx.command.sub.PluginSubcommand;
import io.github.spleefx.extension.standard.splegg.SpleggExtension;
import io.github.spleefx.util.game.Chat;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SpleggMaterialsCommand extends PluginSubcommand {

    private static final List<String> TABS = Arrays.asList("add", "remove", "list");
    private static final Permission PERMISSION = new Permission("spleefx.splegg.materials");

    public SpleggMaterialsCommand() {
        super("materials", c -> PERMISSION, "Edit a list of destroyable/undestroyable splegg materials", c ->
                "/splegg materials <add/remove/list> <arena> <materials...>");
    }

    @Override
    public boolean handle(Command command, CommandSender sender, String[] args) {
        if (args.length < 2)
            throw new CommandException("&cInvalid usage. Try &e" + getUsage(command), SpleggExtension.EXTENSION);
        GameArena arena = GameArena.getByKey(args[1]);
        if (arena == null)
            throw new CommandException("&cInvalid arena: &e" + args[1] + "&c.", SpleggExtension.EXTENSION);
        if (!(arena instanceof SpleggArena))
            throw new CommandException("&cThe specified arena is not a splegg arena!", SpleggExtension.EXTENSION);
        int oldSize = ((SpleggArena) arena).getMaterials().size();
        switch (args[0]) {
            case "add":
                if (args.length == 2)
                    throw new CommandException("&cSpecify materials!", SpleggExtension.EXTENSION);
                for (int i = 2; i < args.length; i++) {
                    Material m = Material.matchMaterial(args[i]);
                    if (m == null)
                        throw new CommandException("&cInvalid material: &e" + args[i] + "&c.", SpleggExtension.EXTENSION);
                    if (((SpleggArena) arena).getMaterials().add(m))
                        Chat.prefix(sender, arena, "&a+ &7" + m.name().toLowerCase());
                }
                if (oldSize == ((SpleggArena) arena).getMaterials().size())
                    Chat.prefix(sender, arena, "&cNo materials have been modified.");
                break;
            case "remove":
                if (args.length == 2)
                    throw new CommandException("&cSpecify materials!", SpleggExtension.EXTENSION);
                for (int i = 2; i < args.length; i++) {
                    Material m = Material.matchMaterial(args[i]);
                    if (m == null)
                        throw new CommandException("&cInvalid material: &e" + args[i] + "&c.", SpleggExtension.EXTENSION);
                    if (((SpleggArena) arena).getMaterials().remove(m))
                        Chat.prefix(sender, arena, "&c- &7" + m.name().toLowerCase());
                }
                if (oldSize == ((SpleggArena) arena).getMaterials().size())
                    Chat.prefix(sender, arena, "&cNo materials have been modified.");
                break;
            case "list":
                for (Material m : ((SpleggArena) arena).getMaterials())
                    Chat.sendUnprefixed(sender, "&7- &e" + m.name().toLowerCase());
                break;
        }
        return true;
    }

    @Override
    public List<String> onTab(CommandSender sender, Command command, String[] args) {
        switch (args.length) {
            case 0:
                return Collections.emptyList();
            case 1:
                return TABS.stream().filter(c -> c.startsWith(args[0])).collect(Collectors.toList());
            case 2:
                return GameArena.ARENAS.get().values().stream().filter(c -> c instanceof SpleggArena)
                        .map(GameArena::getKey).filter(a -> a.startsWith(args[1])).collect(Collectors.toList());
            default:
                GameArena arena = GameArena.getByKey(args[1]);
                if (!(arena instanceof SpleggArena)) return Collections.emptyList();
                if ("remove".equals(args[0])) {
                    return ((SpleggArena) arena).getMaterials().stream().map(m -> m.name().toLowerCase())
                            .filter(a -> a.startsWith(args[args.length - 1])).collect(Collectors.toList());
                }
                return Collections.emptyList();
        }
    }
}
