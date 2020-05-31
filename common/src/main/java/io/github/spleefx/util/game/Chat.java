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
package io.github.spleefx.util.game;

import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.message.MessageKey;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

/**
 * A little chat utility
 */
public class Chat {

    public static void plugin(CommandSender sender, String message) {
        sender.sendMessage(MessageKey.prefix() + colorize(message));
    }

    public static void sendUnprefixed(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }

    public static void prefix(CommandSender sender, GameArena arena, String message) {
        prefix(sender, arena.getExtension(), message);
    }

    public static void prefix(CommandSender sender, @Nullable GameExtension extension, String message) {
        if (extension != null)
            sendUnprefixed(sender, extension.getChatPrefix() + message);
        else plugin(sender, message);
    }

    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

}