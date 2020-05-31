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
package io.github.spleefx.util.io;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A utility to simplify managing files
 */
public class FileManager<P extends JavaPlugin> {

    private static final List<Character> ILLEGAL_CHARACTERS = new ArrayList<>(Arrays.asList('/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'));

    /**
     * Main class instance
     */
    private P plugin;

    public FileManager(P plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates a replica of an embedded file
     *
     * @param fileName File name to create
     * @return The file instance
     */
    public File createFile(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists())
            plugin.saveResource(fileName, false);
        return file;
    }

    /**
     * Creates a directoy with the specified name
     *
     * @param directory Name of the directory to create
     * @return The created file
     */
    public File createDirectory(String directory) {
        File file = new File(plugin.getDataFolder(), directory);
        try {
            forceMkdir(file);
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void forceMkdir(File directory) throws IOException {
        if (directory.exists()) {
            if (!directory.isDirectory()) {
                throw new IOException("File " + directory + " exists and is " + "not a directory. Unable to create directory.");
            }
        } else if (!directory.mkdirs() && !directory.isDirectory()) {
            throw new IOException("Unable to create directory " + directory);
        }
    }

    /**
     * <pre>
     * Checks if a string is a valid path.
     * Null safe.
     *
     * Calling examples:
     *    isValidPath("c:/test");      //returns true
     *    isValidPath("c:/te:t");      //returns false
     *    isValidPath("c:/te?t");      //returns false
     *    isValidPath("c/te*t");       //returns false
     *    isValidPath("good.txt");     //returns true
     *    isValidPath("not|good.txt"); //returns false
     *    isValidPath("not:good.txt"); //returns false
     * </pre>
     */
    public static boolean isValidPath(String path) {
        if (ILLEGAL_CHARACTERS.stream().anyMatch(p -> path.contains(p.toString())))
            return false;
        try {
            Paths.get(path);
        } catch (InvalidPathException | NullPointerException ex) {
            return false;
        }
        return true;
    }

    public P getPlugin() {
        return plugin;
    }
}
