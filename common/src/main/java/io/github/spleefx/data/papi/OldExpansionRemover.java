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
package io.github.spleefx.data.papi;

import io.github.spleefx.SpleefX;
import lombok.AllArgsConstructor;

import java.io.File;

@AllArgsConstructor
public class OldExpansionRemover implements Runnable {

    private SpleefX plugin;

    @Override public void run() {
        File expansionsFolder = new File(plugin.getDataFolder(), ".." + File.separator + "PlaceholderAPI" + File.separator + "expansions");
        File[] expansions = expansionsFolder.listFiles();
        if (expansions == null) return;
        for (File file : expansions) if (file.getName().startsWith("SpleefX-PAPI")) file.delete();
    }
}
