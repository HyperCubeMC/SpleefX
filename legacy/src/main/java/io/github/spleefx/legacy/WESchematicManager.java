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
package io.github.spleefx.legacy;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.WorldData;
import io.github.spleefx.compatibility.worldedit.NoSchematicException;
import io.github.spleefx.compatibility.worldedit.SchematicManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.*;
import java.util.concurrent.CompletableFuture;

public class WESchematicManager extends SchematicManager {

    public WESchematicManager() {
    }

    /**
     * Creates a new schematic processor
     *
     * @param plugin Plugin instance
     * @param name   Name of the schematic
     */
    protected WESchematicManager(WorldEditPlugin plugin, String name, File directory) {
        super(plugin, name, directory);
    }

    @Override
    public void write(ClipboardHolder clipboard) {
        try (Closer closer = Closer.create()) {
            FileOutputStream fos = closer.register(new FileOutputStream(schematic));
            BufferedOutputStream bos = closer.register(new BufferedOutputStream(fos));
            ClipboardWriter writer = closer.register(ClipboardFormat.SCHEMATIC.getWriter(bos));
            writer.write(clipboard.getClipboard(), clipboard.getWorldData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CompletableFuture<Void> paste(Location loc) throws NoSchematicException {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            World weWorld = new BukkitWorld(loc.getWorld());
            WorldData worldData = weWorld.getWorldData();
            Clipboard clipboard = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(schematic)).read(worldData);
            EditSession extent = WorldEdit.getInstance().getEditSessionFactory().getEditSession(weWorld, -1);
            AffineTransform transform = new AffineTransform();
            ForwardExtentCopy copy = new ForwardExtentCopy(clipboard, clipboard.getRegion(), clipboard.getOrigin(),
                    extent, BukkitUtil.toVector(loc));

            if (!transform.isIdentity()) copy.setTransform(transform);

            copy.setSourceMask(new ExistingBlockMask(clipboard));
            Operations.completeLegacy(copy);
            extent.flushQueue();
            future.complete(null);
        } catch (IOException e) {
            throw new NoSchematicException(schematic.getName());
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
        return future;
    }

    @Override
    public SchematicManager newInstance(WorldEditPlugin plugin, String name, File directory) {
        return new WESchematicManager(plugin, name, directory);
    }

    @Override
    protected Location convertVector(String world, ClipboardHolder clipboardHolder) {
        Vector v = clipboardHolder.getClipboard().getOrigin();
        if (clipboardHolder.getClipboard().getRegion().getWorld() != null)
            world = clipboardHolder.getClipboard().getRegion().getWorld().getName();
        return new Location(Bukkit.getWorld(world), v.getBlockX(), v.getBlockY(), v.getBlockZ());
    }
}
