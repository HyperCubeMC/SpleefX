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
package io.github.spleefx.command.core;

import io.github.spleefx.util.game.Chat;

/**
 * A class for handling the commands actions
 */
@FunctionalInterface
public interface CommandCallback {

    /**
     * Invoked when the command is processed
     *
     * @param context Context of the command (data)
     */
    void onProcess(CommandContext context);

    /**
     * Thrown when an exception occurs in the command. This will stop all the executing process
     * of the command.
     */
    class CommandCallbackException extends RuntimeException {

        public CommandCallbackException() {
            this("");
        }

        /**
         * Creates a new command exception
         *
         * @param message Message to send to the command sender
         */
        public CommandCallbackException(String message) {
            super(Chat.colorize(message));
        }
    }

}
