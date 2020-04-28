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

import io.github.spleefx.extension.ExtensionsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * Represents a plugin's subcommand
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PluginSubcommand {

    /**
     * The command's name
     *
     * @return the command name
     */
    String name();

    /**
     * The command's description
     *
     * @return The description
     */
    String description();

    /**
     * The command's parameters
     *
     * @return The command parameters
     */
    String parameters() default "";

    /**
     * The aliases for this command
     *
     * @return The aliases
     */
    String[] aliases() default {};

    /**
     * Whether does this command require a player to run it
     *
     * @return Whether does this command require a player to run it or not
     */
    boolean requirePlayer() default false;
    
    /**
     * The minimum arguments required to run this command
     *
     * @return The minimum arguments
     */
    int minimumArguments() default 0;

    /**
     * The help menu for this command
     *
     * @return The help menu
     */
    String[] helpMenu() default {};

    /**
     * The permission for this command. Use {@code "{extension}"} as a placeholder for the
     * context's extension
     *
     * @return The permission for this command
     */
    String permission();

    /**
     * The tab completions. Separate each argument with a space, each completion in the same argument with a |,
     * and use @s as non-static references
     *
     * @return The completions
     */
    String tabCompletions() default DEFAULT_COMPLETION;

    /**
     * The permission default access for this command
     *
     * @return The permission's access
     */
    PermissionDefault permissionAccess() default PermissionDefault.OP;

    /**
     * The default tab completion.
     */
    String DEFAULT_COMPLETION = "_default_";

    /**
     * An interface which acts like a converter between specific types
     *
     * @param <R> Type to convert to
     */
    @FunctionalInterface
    interface ParameterResolver<R> {

        /**
         * Resolves the specified object
         *
         * @param argument Argument to fetch from
         * @param context  Context to resolve from
         * @return The resolved object. Can throw exceptions
         */
        R resolve(String argument, CommandContext context);

    }

    /**
     * An interface for providing tab completions for certain keys
     */
    @FunctionalInterface
    interface TabProvider {

        /**
         * Returns the tab completions from the specified context
         *
         * @param context Tab context
         * @return The completions.
         */
        List<String> getTab(TabContext context);
    }

    /**
     * Command tab resolving context
     */
    class TabContext {

        private String[] args;
        private CommandSender sender;
        private Command command;
        private CommandContext fakeContext;
        private CommandHandler handler;

        public TabContext(String[] args, CommandSender sender, CommandWrapper wrapper, Command command, CommandHandler handler) {
            this.args = args;
            this.sender = sender;
            this.command = command;
            this.handler = handler;
            fakeContext = new CommandContext(sender, args, ExtensionsManager.getFromCommand(command.getName()), command, wrapper, handler);
        }

        public String[] getArgs() {
            return args;
        }

        public CommandSender getSender() {
            return sender;
        }

        public CommandHandler getHandler() {
            return handler;
        }

        public CommandContext getFakeContext() {
            return fakeContext;
        }

        public Command getCommand() {
            return command;
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public <R> R resolveFirst(Class<R> type, int index) {
            try {
                return (R) CommandResolvers.RESOLVERS.get(type).resolve(args[index], fakeContext);
            } catch (Exception e) {
                return null;
            }
        }
    }

}
