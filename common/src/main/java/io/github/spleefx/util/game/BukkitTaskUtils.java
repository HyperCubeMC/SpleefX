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
package io.github.spleefx.util.game;

import io.github.spleefx.util.plugin.Protocol;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;

/**
 * A class for accessing CraftBukkit data_old from {@link BukkitTask}
 */
public class BukkitTaskUtils {

    /**
     * Declared by the bukkit API; any task with period -2 is cancelled
     */
    private static final int CANCELLED = -2;

    /**
     * The field which represents the period
     */
    private static Field periodField;

    /**
     * Returns the period of this task
     *
     * @param task Task to get from
     * @return The period
     */
    public static long getPeriod(BukkitTask task) {
        try {
            return periodField.getLong(task);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns whether is the task cancelled or not
     *
     * @param task Task to check for
     * @return Whether is the task cancelled or not
     */
    public static boolean isCancelled(BukkitTask task) {
        if (task == null) return true;
        try {
            return task.isCancelled();
        } catch (NoSuchMethodError e) {
            return getPeriod(task) == CANCELLED;
        }
    }

    static {
        try {
            Class<?> craftTask = Protocol.getCraftBukkitClass("scheduler.CraftTask");
            periodField = craftTask.getDeclaredField("period");
            periodField.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }
}