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

import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.util.game.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import static io.github.spleefx.command.core.CommandCallback.CommandCallbackException;

/**
 * Represents the data of a command
 */
public class CommandContext {

    /**
     * The command sender
     */
    private CommandSender sender;

    /**
     * The command arguments
     */
    private String[] args;

    /**
     * The arena's extension
     */
    private GameExtension extension;

    /**
     * The main Bukkit command
     */
    private Command command;

    /**
     * The command being invoked.
     */
    private CommandWrapper wrapper;

    /**
     * The command handler
     */
    private CommandHandler commandHandler;

    /**
     * Creates a new command context
     *
     * @param sender         The command sender
     * @param args           The command arguments
     * @param extension      The command extension
     * @param command        The internal Bukkit command
     * @param wrapper        The command wrapper this is being ran for
     * @param commandHandler The command handler invoking this command
     */
    public CommandContext(CommandSender sender, String[] args, GameExtension extension, Command command, CommandWrapper wrapper, CommandHandler commandHandler) {
        this.sender = sender;
        this.args = args;
        this.extension = extension;
        this.command = command;
        this.wrapper = wrapper;
        this.commandHandler = commandHandler;
    }

    /**
     * Returns the command sender
     *
     * @return The command sender
     */
    public CommandSender getSender() {
        return sender;
    }

    /**
     * Returns the command arguments
     *
     * @return The arguments
     */
    public String[] getArgs() {
        return args;
    }

    /**
     * Returns the command's extension
     *
     * @return The command extension
     */
    public GameExtension getExtension() {
        return extension;
    }

    /**
     * Returns the internal Bukkit command
     *
     * @return The Bukkit command
     */
    public Command getCommand() {
        return command;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public <R> R resolve(int index, Class<R> type) {
        return resolve(args[index], type);
    }

    @SuppressWarnings("unchecked")
    public <R> R resolve(String argument, Class<R> type) {
        return (R) CommandResolvers.RESOLVERS.get(type).resolve(argument, this);
    }

    /**
     * Does a check to make sure the sender is a player, otherwise stops the command execution
     */
    public void requirePlayer() {
        if (!(sender instanceof Player)) {
            Chat.prefix(sender, extension, MessageKey.NOT_PLAYER.getText());
            throw new CommandCallbackException();
        }
    }

    /**
     * Does a check to make sure a valid amount of arguments is inputted
     *
     * @param length Length to require as a minimum
     */
    public void requireArgs(int length) {
        if (args.length < length)
            invalidUsage();
    }

    /**
     * Throws an invalid usage exception. This stops the command execution
     */
    public void invalidUsage() {
        throw new CommandCallbackException(String.format("&cInvalid usage. Try &e/%s &b%s &d%s", command.getName(), wrapper.name, wrapper.parameters));
    }

    /**
     * Returns the sender as a player, or throws an exception if not a player
     *
     * @return The sender as a player
     */
    public Player player() {
        requirePlayer();
        return (Player) sender;
    }

    /**
     * Does a permission check
     *
     * @param permission Permission to check with. Can be either a {@link Permission} or a {@link String}.
     */
    public void checkPermission(Object permission) {
        if (permission instanceof Permission) {
            if (!sender.hasPermission((Permission) permission)) {
                reply(MessageKey.NO_PERMISSION.getText());
                throw new CommandCallbackException();
            }
        } else if (permission instanceof String) {
            if (!sender.hasPermission((String) permission)) {
                reply(MessageKey.NO_PERMISSION.getText());
                throw new CommandCallbackException();
            }
        } else {
            throw new CommandCallbackException("&cFailed to do permission checks: &eInvalid permission specifier: &b" + permission.getClass().getName());
        }
    }

    /**
     * Replies with the specified message and prefixes it with the appropriate prefix (extension or global as a fallback)
     *
     * @param message Message to reply with
     */
    public void reply(String message) {
        Chat.prefix(sender, extension, message);
    }

    /**
     * Replies with the specified message and prefixes it with the global prefix
     *
     * @param message Message to reply with
     */
    public void replyNormalPrefix(String message) {
        Chat.plugin(sender, message);
    }

    public void setExtension(GameExtension extension) {
        this.extension = extension;
    }


    /**
     * Combines the strings from the args array, starting from the specified start point
     *
     * @param start Start index
     * @return The combined string
     */
    public String join(int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < args.length; i++)
            builder.append(args[i]).append(" ");
        return builder.toString().trim();
    }

    public static String joinNiceString(Object[] elements) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            String s = elements[i].toString();
            if (i > 0) if (i == elements.length - 1) builder.append(" &7and ");
            else builder.append("&7, ");
            builder.append(s);
        }
        return builder.toString();
    }

}
