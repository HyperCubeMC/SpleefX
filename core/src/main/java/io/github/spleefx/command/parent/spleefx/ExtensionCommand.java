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
package io.github.spleefx.command.parent.spleefx;

import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.spleef.SpleefArena;
import io.github.spleefx.command.core.CommandCallback;
import io.github.spleefx.command.core.CommandContext;
import io.github.spleefx.command.core.PluginSubcommand;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.compatibility.chat.ChatComponent;
import io.github.spleefx.compatibility.chat.ChatEvents.ClickEvent;
import io.github.spleefx.compatibility.chat.ChatEvents.HoverEvent;
import io.github.spleefx.compatibility.chat.ComponentJSON;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.extension.GameExtension.ExtensionType;
import io.github.spleefx.extension.standard.bowspleef.BowSpleefExtension;
import io.github.spleefx.extension.standard.splegg.SpleggExtension;
import org.apache.commons.lang.ArrayUtils;

@PluginSubcommand(
        name = "extensions",
        parameters = "<load | reload | enable | disable> <extension key> [extension type]",
        description = "Manage extensions",
        aliases = {"ext", "extensions"},
        permission = "spleefx.admin.extensions",
        minimumArguments = 2,
        tabCompletions = "load|reload|enable|disable @_extensions @_etypes" // shrug
)
public class ExtensionCommand implements CommandCallback {

    private static final ComponentJSON WARNING = new ComponentJSON();

    /**
     * Invoked when the command is processed
     *
     * @param context Context of the command (data)
     */
    @Override
    public void onProcess(CommandContext context) {
        String[] args = context.getArgs();
        String key = args[1];
        if (args[0].equalsIgnoreCase("load")) {
            GameExtension ex = SpleefX.getPlugin().getExtensionsManager().load(key);
            if (ex == null)
                throw new CommandCallbackException("&cExtension &e" + key + " &cdoes not exist.");
            ChatComponent warning = new ChatComponent().setText("&cNote: " + "&eIf the extension was already loaded, changes were not updated. To update, click this message (as long as you're a player).".replace(" ", " &e"), true)
                    .setHoverAction(HoverEvent.SHOW_TEXT, "Click to run /spleefx extension reload " + key)
                    .setClickAction(ClickEvent.RUN_COMMAND, "/spleefx extension reload " + key);
            CompatibilityHandler.getProtocol().send(WARNING.clear().append(warning), context.getSender());
        } else {
            GameExtension extension = context.resolve(1, GameExtension.class);
            context.setExtension(extension);
            if (args.length < 3)
                args = (String[]) ArrayUtils.add(args, "custom");
            switch (args[0]) {
                case "reload":
                    if (!key.equals(SpleefArena.EXTENSION.getKey()) && !key.equals(BowSpleefExtension.EXTENSION.getKey()) && !key.equals(SpleggExtension.EXTENSION.getKey())) {
                        if (!SpleefX.getPlugin().getExtensions().hasData(key)) {
                            context.reply("&cExtension &e" + key + " &cdoes not exist.");
                            return;
                        }
                        ExtensionType type = ExtensionType.from(args[2]);
                        if (type == null) {
                            context.reply("&cInvalid type: &e" + args[2]);
                            return;
                        }
                        extension.refresh(type);
                        context.reply("&aExtension &e" + key + " &ahas been successfully reloaded.");
                        return;
                    }
                    extension.refresh(ExtensionType.STANDARD);
                    context.reply("&aExtension &e" + key + " &ahas been successfully reloaded.");
                    break;
                case "enable":
                    extension.setEnabled(true);
                    context.reply("&aExtension &e" + key + " &ahas been enabled.");
                    break;
                case "disable":
                    extension.setEnabled(false);
                    context.reply("&cExtension &e" + key + " &chas been disabled.");
                    break;
            }
        }
    }
}