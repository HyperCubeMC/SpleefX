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
package io.github.spleefx.arena.splegg;

import com.google.common.util.concurrent.AtomicDouble;
import io.github.spleefx.SpleefX;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpleggDelayHandler {

    private boolean started;

    private BukkitTask delayTask;

    private Map<UUID, AtomicDouble> delays = new HashMap<>();

    public void start() {
        if (!started) {
            started = true;
            delayTask = Bukkit.getScheduler().runTaskTimer(SpleefX.getPlugin(), this::reduce, 2, 2);
        }
    }

    public void cancel() {
        delayTask.cancel();
    }

    public boolean has(Player player) {
        return delays.containsKey(player.getUniqueId());
    }

    public void delay(Player player, double delay) {
        if (delay == 0) return;
        delays.put(player.getUniqueId(), new AtomicDouble(delay));
    }

    private void reduce() {
        delays.values().removeIf(a -> a.addAndGet(-0.1) <= 0);
    }

}
