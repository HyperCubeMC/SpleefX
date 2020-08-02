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
package io.github.spleefx.team;

import io.github.spleefx.util.item.Items;
import io.github.spleefx.util.message.message.Message;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents all team colors
 */
public enum TeamColor {

    /**
     * Represents an invalid team color.
     */
    INVALID("Invalid", null, false),

    /**
     * The FFA color
     */
    FFA("FFA", Message.FFA_COLOR, false),

    /**
     * Represents the Red team
     */
    RED("Red", Message.RED, Items.RED_TEAM),

    /**
     * Represents the Green team
     */
    GREEN("Green", Message.GREEN, Items.GREEN_TEAM),

    /**
     * Represents the Blue team
     */
    BLUE("Blue", Message.BLUE, Items.BLUE_TEAM),

    /**
     * Represents the Yellow team
     */
    YELLOW("Yellow", Message.YELLOW, Items.YELLOW_TEAM),

    /**
     * Represents the Pink team
     */
    PINK("Pink", Message.PINK, Items.PINK_TEAM),

    /**
     * Represents the Gray team
     */
    GRAY("Gray", Message.GRAY, Items.GRAY_TEAM);

    /**
     * A human-friendly name of the color
     */
    private final String name;

    /**
     * The chat color represented by this team color
     */
    private String chatColor;

    private final Message key;

    /**
     * A map of all colors
     */
    private static final Map<String, TeamColor> COLORS = new HashMap<>();

    public static final TeamColor[] values = values();

    private final boolean usable;

    private final ItemStack guiItem;

    /**
     * Initiates a new color
     *
     * @param name Name of the color
     * @param key  Message key of the color
     */
    TeamColor(String name, Message key) {
        this(name, key, true);
    }

    /**
     * Initiates a new color
     *
     * @param name   Name of the color
     * @param key    Message key of the color
     * @param usable Whether can the team be maintained by the user or not
     */
    TeamColor(String name, Message key, boolean usable) {
        this.name = name;
        this.key = key;
        this.usable = usable;
        guiItem = null;
    }

    /**
     * Initiates a new color
     *
     * @param name Name of the color
     * @param key  Message key of the color
     */
    TeamColor(String name, Message key, ItemStack guiItem) {
        this.name = name;
        chatColor = getChatColor();
        this.key = key;
        usable = true;
        this.guiItem = guiItem;
    }

    public String getName() {
        return name;
    }

    public String chat() {
        return this == INVALID ? "§0Invalid" : key.getValue();
    }

    @Override
    public String toString() {
        return name;
    }

    public ItemStack getGuiItem() {
        return guiItem;
    }

    public String getChatColor() {
        ChatColor c = null;
        if (key != null)
            try {
                c = ChatColor.getByChar(key.getValue().charAt(1));
            } catch (Exception e) {
                c = ChatColor.UNDERLINE;
            }
        if (c == null)
            chatColor = "";
        else
            chatColor = c.toString();
        return chatColor;
    }

    public static TeamColor get(String name) {
        return COLORS.getOrDefault(name.toLowerCase(), INVALID);
    }

    @Nullable
    public static TeamColor getNullable(String name) {
        return COLORS.get(name.toLowerCase());
    }

    static {
        Arrays.stream(values()).forEachOrdered(color -> COLORS.put(color.getName().toLowerCase(), color));
    }

    public boolean isUsable() {
        return usable;
    }
}
