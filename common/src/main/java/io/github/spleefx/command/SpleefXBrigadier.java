package io.github.spleefx.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.spleefx.SpleefX;
import io.github.spleefx.config.SpleefXConfig;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import me.lucko.commodore.file.CommodoreFileFormat;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.io.InputStream;

public class SpleefXBrigadier {

    public static void register(SpleefX plugin, Command pluginCommand) throws Exception {
        if (!SpleefXConfig.EXPR_HOOK_INTO_BRIGADIER.get() || !CommodoreProvider.isSupported()) return;
        Commodore commodore = CommodoreProvider.getCommodore(plugin);
        try (InputStream is = plugin.getResource("brigadier/spleefx.spleef.commodore")) {
            if (is == null) {
                throw new Exception("Brigadier command data missing from jar");
            }

            LiteralCommandNode<?> commandNode = CommodoreFileFormat.parse(is);
            commodore.register(pluginCommand, commandNode, Player::isOp);
        }
    }
}
