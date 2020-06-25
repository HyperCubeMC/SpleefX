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

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.extension.ability.DoubleJumpHandler;
import io.github.spleefx.scoreboard.ScoreboardHolder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Represents a custom extension mode.
 */
@Getter
@EqualsAndHashCode
@ToString
public class GameExtension {

    @Expose
    private boolean enabled; // DONE

    @Expose
    private String key;

    @Expose
    private String displayName;

    @Expose
    private String chatPrefix; // DONE

    @Expose
    private Map<Integer, Map<SenderType, List<String>>> runCommandsForFFAWinners = Collections.emptyMap();

    @Expose
    private Map<Integer, Map<SenderType, List<String>>> runCommandsForTeamWinners = Collections.emptyMap();

    @Expose
    private boolean preventItemDropping = true;

    @Expose
    private boolean giveDroppedItems = true;

    @Expose
    private List<PotionEffect> givePotionEffects; // DONE

    @Expose
    @SerializedName("doubleJump")
    private DoubleJumpHandler.DataHolder doubleJumpSettings;

    @Expose
    private Map<Integer, ItemHolder> itemsToAdd = Collections.emptyMap(); // DONE

    @Expose
    private Map<ArmorSlots, ItemHolder> armorToAdd = Collections.emptyMap(); // DONE

    @Expose
    private Map<GameEvent, ExtensionTitle> gameTitles = new LinkedHashMap<>(); // DONE

    @Expose
    private List<String> signs = Collections.emptyList(); // DONE

    @Expose
    private GameMode waitingMode = GameMode.ADVENTURE; // DONE

    @Expose
    private GameMode ingameMode = GameMode.ADVENTURE; // DONE

    @Expose
    private List<DamageCause> cancelledDamageInWaiting = Collections.emptyList(); // DONE

    @Expose
    private List<DamageCause> cancelledDamageInGame = Collections.emptyList(); // DONE

    @Expose
    private List<String> extensionCommands = Collections.emptyList(); // DONE

    @Expose
    private List<String> allowedCommands = Collections.emptyList(); // DONE

    @Expose
    private Map<ScoreboardType, ScoreboardHolder> scoreboard = Collections.emptyMap(); // DONE

    @Expose
    private QuitItem quitItem; // DONE

    @Expose
    private List<String> runCommandsWhenGameFills = new ArrayList<>(); // DONE

    @Expose
    private List<String> runCommandsWhenGameStarts = new ArrayList<>(); // DONE

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void refresh(ExtensionType type) {
        GameExtension copy = ExtensionsManager.getExtension(key, type, getClass());
        List<Field> fields = getAllFields(getClass());

        for (Field f : fields)
            try {
                if (Modifier.isStatic(f.getModifiers()) || !f.isAnnotationPresent(Expose.class)) continue;
                f.setAccessible(true);
                f.set(this, f.get(copy));
            } catch (IllegalAccessException e) {
                SpleefX.logger().warning("Failed to reload extension");
                e.printStackTrace();
                break;
            }
    }

    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

    public enum ExtensionType {
        STANDARD,
        CUSTOM;

        private static final Map<String, ExtensionType> TYPES = new HashMap<>();

        public static ExtensionType from(String value) {
            return TYPES.get(value.toUpperCase());
        }

        static {
            Arrays.stream(values()).forEach(c -> TYPES.put(c.name(), c));
        }

    }

    public enum SenderType {
        PLAYER {
            @Override public void run(Player player, String command, GameArena arena) {
                ArenaPlayer p = ArenaPlayer.adapt(player);
                command = ScoreboardHolder.replacePlaceholders(p, command, arena, Collections.emptyMap());
                player.performCommand(command.replace("{winner}", player.getName()).replace("{player}", player.getName()));
            }
        },
        CONSOLE {
            @Override public void run(Player player, String command, GameArena arena) {
                if (player == null) {
                    command = ScoreboardHolder.replacePlaceholders(null, command, arena, Collections.emptyMap());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                } else {
                    ArenaPlayer p = ArenaPlayer.adapt(player);
                    command = ScoreboardHolder.replacePlaceholders(p, command, arena, Collections.emptyMap());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{winner}", player.getName())
                            .replace("{player}", player.getName()));

                }
            }
        };

        public abstract void run(Player player, String command, GameArena arena);

    }

    public static class StringAdapter implements JsonSerializer<GameExtension>, JsonDeserializer<GameExtension> {

        @Override
        public JsonElement serialize(GameExtension src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.key);
        }

        @Override
        public GameExtension deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return ExtensionsManager.getByKey(json.getAsString());
        }
    }

    public enum ArmorSlots {
        helmet {
            @Override public void set(Player player, ItemStack itemStack) {
                player.getInventory().setHelmet(itemStack);
            }
        },
        chestplate {
            @Override public void set(Player player, ItemStack itemStack) {
                player.getInventory().setChestplate(itemStack);
            }
        },
        leggings {
            @Override public void set(Player player, ItemStack itemStack) {
                player.getInventory().setLeggings(itemStack);
            }
        },
        boots {
            @Override public void set(Player player, ItemStack itemStack) {
                player.getInventory().setBoots(itemStack);
            }
        };

        public abstract void set(Player player, ItemStack itemStack);
    }

    public enum ScoreboardType {
        WAITING_IN_LOBBY,
        COUNTDOWN_AND_WAITING,
        COUNTDOWN_AND_FULL,
        GAME_ACTIVE
    }

    public static class QuitItem extends ItemHolder {

        public QuitItem(ItemHolder clone) {
            if (clone == null) return;
            setType(clone.type);
            setColor(clone.color);
            setCount(clone.count);
            setDisplayName(clone.displayName);
            setLore(clone.lore);
            setEnchantments(clone.enchantments);
            setItemFlags(clone.itemFlags);
            setUnbreakable(clone.unbreakable);
        }

        @Expose public boolean give = true;
        @Expose public int slot = 8;
        @Expose public boolean leaveArena = true;
        @Expose public List<String> runCommandsByPlayer = new ArrayList<>();

    }

}
