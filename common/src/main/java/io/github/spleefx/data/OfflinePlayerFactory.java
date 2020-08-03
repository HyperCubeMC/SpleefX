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
package io.github.spleefx.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.util.plugin.Protocol;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Pattern;

/**
 * A class for creating and retrieving {@link OfflinePlayer} instances which are resolved
 * asynchronously to provide full player info, unlike Bukkit's standard methods which either
 * mask the UUID or nullify the name.
 */
public interface OfflinePlayerFactory {

    OfflinePlayerFactory FACTORY = CompatibilityHandler.create(Protocol.VERSION + ".OfflinePlayerFactoryImpl", () -> null);

    OkHttpClient CLIENT = new OkHttpClient();

    /**
     * The Mojang endpoint
     */
    String ENDPOINT = "https://sessionserver.mojang.com/session/minecraft/profile/%s";

    /**
     * The asynchronous thread pool
     */
    ExecutorService THREAD_POOL = new ForkJoinPool();

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
        return CompletableFuture.completedFuture(player);
    }

    /**
     * Requests the player from the Mojang API
     *
     * @param uuid UUID of the player
     * @return A future of the player, after injecting it into Bukkit
     */
    default CompletableFuture<OfflinePlayer> requestPlayer(UUID uuid) {
        if (!Bukkit.getOnlineMode()) return CompletableFuture.completedFuture(Bukkit.getOfflinePlayer(uuid));
        CompletableFuture<OfflinePlayer> future = new CompletableFuture<>();
        THREAD_POOL.submit(() -> {
            try {
                String url = String.format(ENDPOINT, uuid.toString().replace("-", ""));
                Request request = new Request.Builder()
                        .url(url)
                        .header("Content-Type", "application/json")
                        .addHeader("Accept", "application/json")
                        .build();
                try (Response response = CLIENT.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseText = Objects.requireNonNull(response.body()).string();
                        ProfileResponse p = GSON.fromJson(responseText, ProfileResponse.class);
                        OfflinePlayer player = injectPlayer(p.id, p.name);
                        future.complete(player);
                    } else {
                        future.complete(injectPlayer(uuid, "NoName"));
                    }
                }
            } catch (IOException e) {
                future.obtrudeException(e);
            }
        });
        return future;
    }

    /**
     * Registers the player into Bukkit's offline player cache
     *
     * @param uuid     UUID of the player
     * @param username Username of the player
     * @return The created player
     */
    OfflinePlayer injectPlayer(UUID uuid, String username);

    // it's used by gson but whatever
    @SuppressWarnings("unused")
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
