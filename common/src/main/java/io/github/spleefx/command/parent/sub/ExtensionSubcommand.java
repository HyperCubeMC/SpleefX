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
package io.github.spleefx.command.parent.sub;

import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.spleef.SpleefArena;
import io.github.spleefx.command.sub.PluginSubcommand;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.compatibility.chat.ChatComponent;
import io.github.spleefx.compatibility.chat.ChatEvents.ClickEvent;
import io.github.spleefx.compatibility.chat.ChatEvents.HoverEvent;
import io.github.spleefx.compatibility.chat.ComponentJSON;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.extension.GameExtension.ExtensionType;
import io.github.spleefx.extension.standard.bowspleef.BowSpleefExtension;
import io.github.spleefx.extension.standard.splegg.SpleggExtension;
import io.github.spleefx.util.game.Chat;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ExtensionSubcommand extends PluginSubcommand {

    private static final List<String> ARGS_1 = Arrays.asList("load", "reload", "enable", "disable");
    private static final List<String> TYPES = Arrays.asList("standard", "custom");

    private static final ComponentJSON WARNING = new ComponentJSON();

    private static final Permission PERMISSION = new Permission("spleefx.admin.extensions");
    private static final List<String> ALIASES = Collections.singletonList("ext");
    private static final List<String> HELP = Arrays.asList(
            "&eextension &cload &a<extension> &7- &dLoad an extension into memory",
            "&eextension &creload &a<extension> <extension type> &7- &dReload an extension to apply changes",
            "&eextension &cenable &a<extension> &7- &dEnable an extension",
            "&eextension &cdisable &a<extension> &7- &dDisable an extension and all its arenas"
    );

    public ExtensionSubcommand() {
        super("extension", (c) -> PERMISSION, "Manage extensions", (c) -> "/spleefx extension <load | reload | enable | disable> <extension key> [extension type]");
        super.aliases = ALIASES;
        super.helpMenu = HELP;
    }

    @Override
    public boolean handle(Command command, CommandSender sender, String[] args) {
        if (args.length < 2)
            return false;
        String key = args[1];
        GameExtension e = ExtensionsManager.getByKey(key);
        if (e == null && !args[0].equalsIgnoreCase("load")) {
            Chat.plugin(sender, "&cExtension &e" + key + " &cdoes not exist.");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "load": {
                GameExtension ex = SpleefX.getPlugin().getExtensionsManager().load(key);
                if (ex == null) {
                    Chat.plugin(sender, "&cExtension &e" + key + " &cdoes not exist.");
                    return true;
                }
                Chat.prefix(sender, ex, "&aExtension &e" + key + " &ahas been successfully loaded.");
                ChatComponent warning = new ChatComponent().setText("&cNote: " + "&eIf the extension was already loaded, changes were not updated. To update, click this message (as long as you're a player).".replace(" ", " &e"), true)
                        .setHoverAction(HoverEvent.SHOW_TEXT, "Click to run /spleefx extension reload " + key)
                        .setClickAction(ClickEvent.RUN_COMMAND, "/spleefx extension reload " + key);
                CompatibilityHandler.getProtocol().send(WARNING.clear().append(warning), sender);
            }
            return true;
            case "reload":
                if (args.length < 3)
                    args = (String[]) ArrayUtils.add(args, "custom");
                if (!key.equals(SpleefArena.EXTENSION.getKey()) && !key.equals(BowSpleefExtension.EXTENSION.getKey()) && !key.equals(SpleggExtension.EXTENSION.getKey())) {
                    if (!SpleefX.getPlugin().getExtensions().hasData(key)) {
                        Chat.plugin(sender, "&cExtension &e" + key + " &cdoes not exist.");
                        return true;
                    }
                    ExtensionType type = ExtensionType.from(args[2]);
                    if (type == null) {
                        Chat.plugin(sender, "&cInvalid type: &e" + args[2]);
                        return true;
                    }
                    e.refresh(type);
                    Chat.prefix(sender, e, "&aExtension &e" + key + " &ahas been successfully reloaded.");
                    return true;
                }
                e.refresh(ExtensionType.STANDARD);
                Chat.prefix(sender, e, "&aExtension &e" + key + " &ahas been successfully reloaded.");
                return true;
            case "enable":
                e.setEnabled(true);
                Chat.prefix(sender, e, "&aExtension &e" + key + " &ahas been enabled.");
                return true;
            case "disable":
                e.setEnabled(false);
                Chat.prefix(sender, e, "&cExtension &e" + key + " &chas been disabled.");
                return true;
        }
        return false;
    }

    @Override
    public List<String> onTab(CommandSender sender, Command command, String[] args) {
        if (!sender.hasPermission(getPermission(command))) return Collections.emptyList();
        switch (args.length) {
            case 0:
                return Collections.emptyList();
            case 1:
                return ARGS_1.stream().filter(a -> a.startsWith(args[0])).collect(Collectors.toList());
            case 2:
                switch (args[0]) {
                    case "disable":
                        return ExtensionsManager.EXTENSIONS.values().stream().filter(GameExtension::isEnabled).map(GameExtension::getKey).filter(c -> c.startsWith(args[1])).collect(Collectors.toList());
                    case "enable":
                        return ExtensionsManager.EXTENSIONS.values().stream().filter(e -> !e.isEnabled()).map(GameExtension::getKey).filter(c -> c.startsWith(args[1])).collect(Collectors.toList());
                    case "reload":
                        return ExtensionsManager.EXTENSIONS.keySet().stream().filter(c -> c.startsWith(args[1])).collect(Collectors.toList());
                    default:
                        return Collections.emptyList();
                }
            default:
                if (args.length == 3 && args[0].equalsIgnoreCase("reload"))
                    return TYPES.stream().filter(a -> a.startsWith(args[2])).collect(Collectors.toList());
                return Collections.emptyList();
        }
    }
}
