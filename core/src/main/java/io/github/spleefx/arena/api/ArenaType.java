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
package io.github.spleefx.arena.api;

import io.github.spleefx.command.sub.CommandException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an arena type
 */
public enum ArenaType {

    /**
     * A Free-for-all type, no teams, each player has their own spawnpoint
     */
    FREE_FOR_ALL("ffa", "freeforall", "free_for_all"),

    /**
     * A teams type, each team has its own spawnpoint
     */
    TEAMS("teams", "team");

    private final String[] aliases;

    ArenaType(String... aliases) {
        this.aliases = aliases;
    }

    private static final Map<String, ArenaType> ALIASES = new HashMap<>();

    public static ArenaType lookup(String name) {
        ArenaType type = ALIASES.get(name);
        if (type == null)
            throw new CommandException("&cInvalid arena type: &e" + name + "&c. Available types: &bffa&c, &bteams&c.");
        return type;
    }

    static {
        Arrays.stream(values()).forEachOrdered(type -> Arrays.stream(type.aliases).forEach(alias -> ALIASES.put(alias, type)));
    }

}
