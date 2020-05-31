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
package io.github.spleefx.arena;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.session.ClipboardHolder;
import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.api.ArenaEngine;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.compatibility.worldedit.NoSchematicException;
import io.github.spleefx.compatibility.worldedit.SchematicManager;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.util.game.Chat;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * A class for managing arenas
 */
public class ArenaManager {

    /**
     * Main plugin instance
     */
    private SpleefX plugin;

    public ArenaManager(SpleefX plugin) {
        this.plugin = plugin;
    }

    public <T extends GameArena> T add(Player player, T arena, String command) {
        try {
            SchematicManager processor = SpleefX.newSchematicManager(arena.getKey());
            ClipboardHolder clipboard = plugin.getWorldEdit().getSession(player).getClipboard();
            processor.write(clipboard);
            if (arena.getRegenerationPoint() == null) throw new EmptyClipboardException();
            GameArena.ARENAS.get().put(arena.getKey(), arena);
            MessageKey.ARENA_CREATED.send(player, arena, null, null, null, command,
                    null, -1, arena.getExtension());
            return arena;
        } catch (EmptyClipboardException e) {
            Chat.prefix(player, arena, "&cYou must select and copy the arena to your clipboard (with WorldEdit)!");
            GameArena.ARENAS.get().remove(arena.getKey());
        }
        return null;
    }

    /**
     * Deletes an arena and all its data
     *
     * @param key Key of the arena to remove
     * @return The previous arena
     */
    public GameArena removeArena(String key) {
        GameArena arena = GameArena.ARENAS.get().remove(key);
        if (arena == null) return null;
        Preconditions.checkState(arena.getEngine().getArenaStage() != ArenaStage.ACTIVE, "The arena has running games! Wait until games are done.");
        File schem = new File(plugin.getArenasFolder(), key + ".schem");
        schem.delete();
        return arena;
    }

    /**
     * Regenerates the specified arena.
     * <p>
     * Note: It is not recommended to use this method directly. Use {@link ArenaEngine#regenerate()}.
     *
     * @param key Arena key to regenerate
     */
    public CompletableFuture<Void> regenerateArena(String key) throws NoSchematicException {
        SchematicManager processor = SpleefX.newSchematicManager(key);
        return processor.paste(GameArena.getByKey(key).getRegenerationPoint());
    }
}
