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
package io.github.spleefx.data.leaderboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.util.plugin.Protocol;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * A class for creating and retrieving {@link OfflinePlayer} instances which are resolved
 * asynchronously to provide full player info, unlike Bukkit's standard methods which either
 * mask the UUID or nullify the name.
 */
public interface OfflinePlayerFactory {

    OfflinePlayerFactory FACTORY = CompatibilityHandler.create(Protocol.VERSION + ".OfflinePlayerFactoryImpl", () -> null);

    /**
     * The Mojang endpoint
     */
    String ENDPOINT = "https://sessionserver.mojang.com/session/minecraft/profile/%s";

    /**
     * The asynchronous thread pool
     */
    ExecutorService THREAD_POOL = Executors.newSingleThreadExecutor();

    /**
     * Gson to deserialize response data
     */
    Gson GSON = new GsonBuilder()
            .registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
            .create();

    /**
     * Retrieves the player from the Bukkit cache, or requests it asynchronously from the Mojang API.
     *
     * @param uuid UUID to resolve from
     * @return A possibly-unfinished future of the player.
     */
    default CompletableFuture<OfflinePlayer> getOrRequest(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.getName() == null) return requestPlayer(uuid);
        CompletableFuture<OfflinePlayer> future = new CompletableFuture<>();
        future.complete(player);
        return future;
    }

    CompletableFuture<OfflinePlayer> requestPlayer(UUID uuid);

    // it's used by gson but whatever
    @SuppressWarnings("unused")
    @Getter
    class ProfileResponse {

        private UUID id;
        private String name;

    }

    class UUIDTypeAdapter extends TypeAdapter<UUID> {

        private static final Pattern STRIPPED_UUID_PATTERN = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

        public void write(JsonWriter out, UUID value) throws IOException {
            out.value(fromUUID(value));
        }

        public UUID read(JsonReader in) throws IOException {
            return fromString(in.nextString());
        }

        public static String fromUUID(UUID uuid) {
            return uuid.toString().replace("-", "");
        }

        public static UUID fromString(String uuid) {
            return UUID.fromString(STRIPPED_UUID_PATTERN.matcher(uuid).replaceAll("$1-$2-$3-$4-$5"));
        }
    }



}
