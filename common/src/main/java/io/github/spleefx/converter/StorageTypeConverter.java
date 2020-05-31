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
package io.github.spleefx.converter;

import io.github.spleefx.arena.api.ArenaData;
import io.github.spleefx.data.DataProvider;
import io.github.spleefx.data.GameStats;
import io.github.spleefx.data.provider.UnitedFileProvider;

public class StorageTypeConverter implements Runnable {

    private DataProvider newProvider;

    public StorageTypeConverter(DataProvider newProvider) {
        this.newProvider = newProvider;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        UnitedFileProvider fileProvider = new UnitedFileProvider();
        if (!fileProvider.getConfig().getFile().exists()) return;
        fileProvider.getConfig().getContent().entrySet().forEach((entry) -> {
            String player = entry.getKey();
            GameStats stats = ArenaData.GSON.fromJson(entry.getValue(), GameStats.class);
            newProvider.setStatistics(DataProvider.getStoringStrategy().from(player), stats);
        });
        fileProvider.getConfig().getFile().getFile().delete();
    }
}
