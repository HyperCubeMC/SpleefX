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
package io.github.spleefx.compatibility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.spleefx.SpleefX;
import io.github.spleefx.compatibility.chat.ChatComponent;
import io.github.spleefx.compatibility.chat.ChatComponent.Adapter;
import io.github.spleefx.compatibility.chat.ComponentJSON;
import io.github.spleefx.compatibility.reflect.ReflectiveProtocolNMS;
import io.github.spleefx.util.game.ExplosionSettings;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.moltenjson.json.JsonBuilder;

import java.util.Random;

/**
 * An interface to abstract NMS access
 */
public interface ProtocolNMS {

    /**
     * The fallback NMS handler
     */
    ProtocolNMS FALLBACK = new ReflectiveProtocolNMS();

    /**
     * Random used for explosions
     */
    Random RANDOM = new Random();

    /**
     * The JSON text
     */
    String TITLE_TEXT = new JsonBuilder().map("text", "%s").build();

    /**
     * The GSON used for serializing and deserializing
     */
    Gson CHAT_GSON = new GsonBuilder().registerTypeAdapter(ChatComponent.class, new Adapter()).create();

    /**
     * The action bar ID in old protocols
     */
    byte ACTION_BAR_ID = 2;

    /**
     * Displays the title
     *
     * @param player   Player to display for
     * @param title    Main title to display
     * @param subtitle Subtitle to display
     * @param fadeIn   Ticks in which the title will be fading into the screen
     * @param display  Ticks in which the title will display on the screen
     * @param fadeOut  Ticks in which the title will be fading out of the screen
     */
    void displayTitle(Player player, String title, String subtitle, int fadeIn, int display, int fadeOut);

    /**
     * Sends the component to the specified player
     *
     * @param component Component to send
     * @param player    Player to send for
     */
    void send(ComponentJSON component, CommandSender player);

    /**
     * Displays the specified action bar
     *
     * @param player    Player to display for
     * @param actionBar The text
     */
    void displayActionBar(Player player, String actionBar);

    /**
     * Creates an explosion
     *
     * @param location Location to create for
     * @param settings Explosion settings
     */
    void createExplosion(Location location, ExplosionSettings settings);

    /**
     * Gets the squared distance to the position.
     */
    default double getDistanceSq(double posX, double posY, double posZ, double x, double y, double z) {
        double d0 = posX - x;
        double d1 = posY - y;
        double d2 = posZ - z;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    default void hidePlayer(Player toHide, Player target) {
        toHide.hidePlayer(SpleefX.getPlugin(), target);
    }
    default void showPlayer(Player toHide, Player target) {
        toHide.showPlayer(SpleefX.getPlugin(), target);
    }

}
