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
package io.github.spleefx.compatibility.reflect;

import io.github.spleefx.SpleefX;
import io.github.spleefx.compatibility.ProtocolNMS;
import io.github.spleefx.compatibility.chat.ComponentJSON;
import io.github.spleefx.util.code.Reflect;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.game.ExplosionSettings;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static io.github.spleefx.util.plugin.Protocol.getCraftBukkitClass;
import static io.github.spleefx.util.plugin.Protocol.getProtocolClass;

@SuppressWarnings("unchecked")
public class ReflectiveProtocolNMS implements ProtocolNMS {

    private static boolean disable = false;

    @Override
    public void displayTitle(Player player, String title, String subtitle, int fadeIn, int display, int fadeOut) {
        if (disable) return;
        try {
            Object entityPlayer = getHandle.invoke(player);
            Object connection = playerConnection.get(entityPlayer);
            Object resetPacket = packetTitle.newInstance(resetEnum, null);
            sendPacket.invoke(connection, resetPacket);
            if (StringUtils.isNotEmpty(title)) {
                Object chatComponent = serialize.invoke(null, String.format(TITLE_TEXT, Chat.colorize(title)));
                Object packet = packetTitle.newInstance(titleEnum, chatComponent);
                sendPacket.invoke(connection, packet);
            }
            if (StringUtils.isNotEmpty(subtitle)) {
                Object chatComponent = serialize.invoke(null, String.format(TITLE_TEXT, Chat.colorize(subtitle)));
                Object packet = packetTitle.newInstance(subtitleEnum, chatComponent);
                sendPacket.invoke(connection, packet);
            }
            Object length = packetLength.newInstance(fadeIn, display, fadeOut);
            sendPacket.invoke(connection, length);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(ComponentJSON component, CommandSender player) {
        if (disable || !(player instanceof Player)) player.sendMessage(Chat.colorize(component.getStripped()));
        else {
            try {
                Object entityPlayer = getHandle.invoke(player);
                Object packet = packetPlayOutChat.newInstance(serialize.invoke(null, component.toString()));
                sendPacket.invoke(playerConnection.get(entityPlayer), packet);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void createExplosion(Location location, ExplosionSettings settings) {
    }

    @Override
    public void displayActionBar(Player player, String text) {
    }

    // @formatter:off
    private static Method getHandle, serialize, sendPacket;
    private static Field playerConnection;
    private static Constructor packetTitle, packetLength;
    private static Enum titleEnum, subtitleEnum, resetEnum;
    private static Constructor packetPlayOutChat;
    //@formatter:on

    static {
        try {
            getHandle = Reflect.method(getCraftBukkitClass("entity.CraftPlayer"), "getHandle");
            serialize = Reflect.method(getProtocolClass("IChatBaseComponent$ChatSerializer"), "a", String.class);
            sendPacket = Reflect.method(getProtocolClass("PlayerConnection"), "sendPacket", getProtocolClass("Packet"));
            playerConnection = getProtocolClass("EntityPlayer").getDeclaredField("playerConnection");
            Class packetPlayOutTitle = getProtocolClass("PacketPlayOutTitle");
            Class enumTitleAction = getProtocolClass("PacketPlayOutTitle$EnumTitleAction");
            packetTitle = packetPlayOutTitle.getDeclaredConstructor(enumTitleAction, getProtocolClass("IChatBaseComponent"));
            packetLength = packetPlayOutTitle.getDeclaredConstructor(int.class, int.class, int.class);
            titleEnum = Enum.valueOf(enumTitleAction, "TITLE");
            subtitleEnum = Enum.valueOf(enumTitleAction, "SUBTITLE");
            resetEnum = Enum.valueOf(enumTitleAction, "RESET");
            packetPlayOutChat = getProtocolClass("PacketPlayOutChat").getDeclaredConstructor(getProtocolClass("IChatBaseComponent"));
        } catch (ReflectiveOperationException e) {
            SpleefX.logger().warning("Failed to access required NMS data to send titles. Titles will not be sent");
            disable = true;
        }
    }
}