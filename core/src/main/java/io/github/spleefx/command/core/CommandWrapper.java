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
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * An object wrapper for {@link PluginSubcommand}
 */
public class CommandWrapper {

    // no because we don't want those accessed somewhere else.

    public final String name, description, parameters;
    public final String[] aliases;
    public final List<String> helpMenu;
    public final String permission;
    public final PermissionDefault permissionAccess;
    public final int minimumArgs;
    public final boolean requirePlayer;
    public final String tab;
    public final CommandCallback callback;

    public CommandWrapper(String name,
                          String description,
                          String parameters,
                          String[] aliases,
                          List<String> helpMenu,
                          String permission,
                          PermissionDefault permissionAccess,
                          int minimumArgs,
                          boolean requirePlayer,
                          String tab,
                          CommandCallback callback) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.aliases = aliases;
        this.helpMenu = helpMenu;
        this.permission = permission;
        this.permissionAccess = permissionAccess;
        this.minimumArgs = minimumArgs;
        this.requirePlayer = requirePlayer;
        this.tab = tab;
        this.callback = callback;
    }

    public Permission generatePermission(GameExtension extension) {
        return new Permission(permission.replace("{ext}", extension == null ? "spleefx" : extension.getKey()), permissionAccess);
    }

}