/*
 * * Copyright 2019 github.com/ReflxctionDev
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
package io.github.spleefx.modern;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import io.github.spleefx.compatibility.worldedit.NoSchematicException;
import io.github.spleefx.compatibility.worldedit.SchematicManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class WESchematicManager extends SchematicManager {

    public WESchematicManager() {
    }

    protected WESchematicManager(WorldEditPlugin plugin, String name, File directory) {
        super(plugin, name, directory);
    }

    @Override
    public void write(ClipboardHolder clipboard) {
        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(schematic))) {
            writer.write(clipboard.getClipboard());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CompletableFuture<Void> paste(Location location) throws NoSchematicException {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try (EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(location.getWorld()), -1)) {
            Operation operation = new ClipboardHolder(load())
                    .createPaste(session)
                    .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                    .ignoreAirBlocks(false)
                    .build();
            Operations.complete(operation);
            session.flushSession();
            future.complete(null);
        } catch (WorldEditException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            throw new NoSchematicException(SchematicManager.getBaseName(schematic));
        }
        return future;
    }

    /**
     * Loads the schematic as a clipboard
     *
     * @return The clipboard of the schematic
     */
    Clipboard load() {
        ClipboardFormat format = ClipboardFormats.findByFile(schematic);
        try (ClipboardReader reader = format.getReader(new FileInputStream(schematic))) {
            return reader.read();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected SchematicManager newInstance(WorldEditPlugin plugin, String name, File directory) {
        return new WESchematicManager(plugin, name, directory);
    }

    @Override
    protected Location convertVector(String world, ClipboardHolder clipboardHolder) {
        BlockVector3 v = clipboardHolder.getClipboard().getOrigin();
        if (clipboardHolder.getClipboard().getRegion().getWorld() != null)
            world = clipboardHolder.getClipboard().getRegion().getWorld().getName();
        return new Location(Bukkit.getWorld(world), v.getBlockX(), v.getBlockY(), v.getBlockZ());
    }
}
