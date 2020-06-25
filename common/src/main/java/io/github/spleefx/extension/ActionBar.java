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
package io.github.spleefx.extension;

import com.google.gson.annotations.Expose;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.util.PlaceholderUtil;
import org.bukkit.entity.Player;

/**
 * Represents an action bar
 */
public class ActionBar {

    /**
     * Whether should this be displayed or not
     */
    @Expose
    private boolean enabled;

    /**
     * The action bar text
     */
    @Expose
    private String text;

    public ActionBar(boolean enabled, String text) {
        this.enabled = enabled;
        this.text = text;
    }

    public void display(Player player) {
        if (!enabled) return;
        CompatibilityHandler.getProtocol().displayActionBar(player, text);
    }

    public void display(Player player, Player spec) {
        if (!enabled) return;
        CompatibilityHandler.getProtocol().displayActionBar(player, PlaceholderUtil.all(text, spec));
    }

}
