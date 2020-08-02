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
package io.github.spleefx.extension;

import com.google.gson.annotations.Expose;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.util.PlaceholderUtil;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;

/**
 * Represents the title displayed by an extension
 */
@AllArgsConstructor
public class ExtensionTitle {

    @Expose
    public boolean enabled;

    @Expose
    @Setter
    private String title;

    @Expose
    private String subtitle;

    @Expose
    private int fadeInTicks;

    @Expose
    private int displayTicks;

    @Expose
    private int fadeOutTicks;

    public ExtensionTitle() {
    }

    public ExtensionTitle(ExtensionTitle clone) {
        enabled = clone.enabled;
        title = clone.title;
        subtitle = clone.subtitle;
        fadeInTicks = clone.fadeInTicks;
        displayTicks = clone.displayTicks;
        fadeOutTicks = clone.fadeOutTicks;
    }

    public void display(Player player, String winner) {
        try {
            if (enabled)
                CompatibilityHandler.getProtocol().displayTitle(player, title.replace("{winner}", winner), subtitle.replace("{winner}", winner), fadeInTicks, displayTicks, fadeOutTicks);
        } catch (Throwable ignored) {
        }
    }

    public void display(Player player, Player target) {
        try {
            if (enabled)
                CompatibilityHandler.getProtocol().displayTitle(player, PlaceholderUtil.all(title, target), PlaceholderUtil.all(subtitle, target), fadeInTicks, displayTicks, fadeOutTicks);
        } catch (Throwable ignored) {
        }
    }

    public void display(Player player) {
        try {
            if (enabled)
                CompatibilityHandler.getProtocol().displayTitle(player, title, subtitle, fadeInTicks, displayTicks, fadeOutTicks);
        } catch (Throwable ignored) {
        }
    }

}