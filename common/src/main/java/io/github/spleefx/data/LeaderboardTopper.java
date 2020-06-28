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

import io.github.spleefx.data.leaderboard.OfflinePlayerFactory;
import org.bukkit.OfflinePlayer;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a leaderboard topper
 */
public class LeaderboardTopper {

    private UUID player;
    private int count;

    private OfflinePlayer playerOff;

    public LeaderboardTopper(UUID player, int count) {
        this.player = player;
        this.count = count;
    }

    public CompletableFuture<OfflinePlayer> getPlayer() {
        return playerOff == null ? OfflinePlayerFactory.FACTORY.getOrRequest(player).thenApply(p -> playerOff = p)
                : CompletableFuture.completedFuture(playerOff);
    }

    public int getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeaderboardTopper that = (LeaderboardTopper) o;
        return Objects.equals(player, that.player);
    }

    @Override public int hashCode() {
        return Objects.hash(player);
    }
}
