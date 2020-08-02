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
package io.github.spleefx.v1_8_R3;

import com.mojang.authlib.GameProfile;
import io.github.spleefx.data.OfflinePlayerFactory;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

import java.util.UUID;

public class OfflinePlayerFactoryImpl implements OfflinePlayerFactory {

    @Override public OfflinePlayer injectPlayer(UUID uuid, String username) {
        return ((CraftServer) Bukkit.getServer()).getOfflinePlayer(new GameProfile(uuid, username));
    }

}
