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
package io.github.spleefx.command.parent.extensions;

import io.github.spleefx.command.core.CommandCallback;
import io.github.spleefx.command.core.CommandContext;
import io.github.spleefx.command.core.CommandWrapper;
import io.github.spleefx.command.core.PluginSubcommand;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.compatibility.chat.ChatComponent;
import io.github.spleefx.compatibility.chat.ChatEvents.ClickEvent;
import io.github.spleefx.compatibility.chat.ChatEvents.HoverEvent;
import io.github.spleefx.compatibility.chat.ComponentJSON;

@PluginSubcommand(
        name = "help",
        parameters = "[subcommand]",
        aliases = {"?"},
        tabCompletions = "@commands",
        permission = "spleefx.{ext}.help",
        helpMenu = {"&ehelp &a[subcommand] &7- &dGet help for a specific subcommand"},
        description = "Display the help menu"
)
public class HelpCommand implements CommandCallback {

    /**
     * Invoked when the command is processed
     *
     * @param context Context of the command (data)
     */
    @Override
    public void onProcess(CommandContext context) {
        String[] args = context.getArgs();
        if (context.getArgs().length == 0) {
            for (CommandWrapper command : context.getCommandHandler().getNamesOnly().values()) {
                if (!context.getSender().hasPermission(command.generatePermission(context.getExtension()))) continue;
                ComponentJSON json = new ComponentJSON();
                json.append(new ChatComponent().setText(
                        context.getExtension().getChatPrefix() + "&e/" + context.getCommand().getName() + "&b " + command.name + " &d" + command.parameters, false).setHoverAction(
                        HoverEvent.SHOW_TEXT, String.join("\n\n", command.helpMenu)
                ).setClickAction(ClickEvent.SUGGEST_COMMAND, "/" + context.getCommand().getName() + " " + command.name));
                CompatibilityHandler.getProtocol().send(json, context.getSender());
            }
            context.reply("&7&lTIP: &eHover over a command for more info, and click to add to your chat box!");
        } else {
            CommandWrapper subcommand = context.getCommandHandler().getCommands().get(args[0]);
            if (subcommand == null) throw new CommandCallbackException("&cInvalid subcommand: &e" + args[0]);
            context.checkPermission(subcommand.generatePermission(context.getExtension()));
            if (subcommand.helpMenu.size() == 0)
                throw new CommandCallbackException("&cNo help menu for this command.");
            else
                for (String help : subcommand.helpMenu) context.reply("&e" + help);
        }
    }
}
