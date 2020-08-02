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
package io.github.spleefx.command.sub;

import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.util.game.Chat;
import org.bukkit.command.CommandSender;

public class CommandException extends RuntimeException {

    private final String message;
    private final GameExtension extension;

    public CommandException(String message) {
        this(message, null);
    }

    /**
     * Constructs a new runtime exception with {@code null} as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public CommandException(String message, GameExtension extension) {
        this.message = message;
        this.extension = extension;
    }

    public void send(CommandSender sender) {
        Chat.prefix(sender, extension, message);
    }
}
