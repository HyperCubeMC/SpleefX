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
package io.github.spleefx.util.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Server;

/**
 * A simple utility for dealing with protocol-specific code
 */
public class Protocol {

    /**
     * The server version, e.g "v1_11_R1"
     */
    public static final String VERSION = getVersion(Bukkit.getServer());

    /**
     * The server's protocol. For example, if 1.11.2 it will be {@code 11}
     */
    public static final int PROTOCOL = Integer.parseInt(VERSION.split("_")[1]);

    /**
     * The exact protocol version. For example, if 1.12.2, it will be {@code 12.2}
     */
    public static final double EXACT = Double.parseDouble(Bukkit.getBukkitVersion().split("-", 2)[0].split(".", 2)[1].substring(1));

    /**
     * Returns whether the current protocol is newer than the specified one
     *
     * @param protocol Protocol to check for
     * @return ^
     */
    public static boolean isNewerThan(int protocol) {
        return PROTOCOL >= protocol;
    }

    /**
     * Returns whether the current protocol is older than the specified one
     *
     * @param protocol Protocol to check for
     * @return ^
     */
    public static boolean isOlderThan(int protocol) {
        return PROTOCOL <= protocol;
    }

    /**
     * Returns the current protocol version, e.g "11" if 1.11.X
     *
     * @return The protocol version
     */
    public static int getCurrentProtocol() {
        return PROTOCOL;
    }

    /**
     * Returns the NMS class from the specified name
     *
     * @param name Name of the class
     * @return The class instance
     */
    public static Class<?> getProtocolClass(String name) {
        try {
            return Class.forName("net.minecraft.server." + VERSION + "." + name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the CraftBukkit class from the specified name
     *
     * @param name Name of the class
     * @return The class instance
     */
    public static Class<?> getCraftBukkitClass(String name) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + VERSION + "." + name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the server protocol version, e.g v1_12_R1
     *
     * @param server Server instance
     * @return The server version
     */
    private static String getVersion(Server server) {
        final String packageName = server.getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }
}
