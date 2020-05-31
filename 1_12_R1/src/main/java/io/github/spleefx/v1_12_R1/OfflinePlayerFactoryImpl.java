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
package io.github.spleefx.v1_12_R1;

import com.mojang.authlib.GameProfile;
import io.github.spleefx.data.leaderboard.OfflinePlayerFactory;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class OfflinePlayerFactoryImpl implements OfflinePlayerFactory {

    @Override
    public CompletableFuture<OfflinePlayer> requestPlayer(UUID uuid) {
        CompletableFuture<OfflinePlayer> future = new CompletableFuture<>();
        THREAD_POOL.submit(() -> {
            try {
                String url = String.format(ENDPOINT, uuid.toString().replace("-", ""));
                URL object = new URL(url);

                HttpURLConnection con = (HttpURLConnection) object.openConnection();
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("GET");

                String responseText;
                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
                    responseText = br.lines().map(line -> line + "\n").collect(Collectors.joining());
                    br.close();
                    ProfileResponse p = GSON.fromJson(responseText, ProfileResponse.class);

                    OfflinePlayer player = ((CraftServer) Bukkit.getServer()).getOfflinePlayer(new GameProfile(p.getId(), p.getName()));

                    future.complete(player);
                } else {
                    future.completeExceptionally(new IllegalStateException(con.getResponseMessage()));
                }
            } catch (Throwable e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

}
