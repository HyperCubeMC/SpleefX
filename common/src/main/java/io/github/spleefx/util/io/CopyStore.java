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
package io.github.spleefx.util.io;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class which stores locations of running //copy and //cut, as to store them for arena regeneration.
 */
public class CopyStore implements Listener {

    private static final List<String> CLIPBOARD_COMMANDS = Arrays.asList("//copy", "//cut");

    public static final Map<Player, Location> LOCATIONS = new HashMap<>();

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (CLIPBOARD_COMMANDS.stream().anyMatch(event.getMessage()::contains))
            LOCATIONS.put(event.getPlayer(), event.getPlayer().getLocation());
    }

}
