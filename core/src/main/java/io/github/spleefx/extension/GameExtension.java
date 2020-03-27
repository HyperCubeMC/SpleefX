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
import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.ArenaStage;
import io.github.spleefx.extension.ability.DoubleJumpHandler;
import io.github.spleefx.extension.ability.DoubleJumpHandler.DataHolder;
import io.github.spleefx.scoreboard.ScoreboardHolder;
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
    private DoubleJumpHandler.DataHolder doubleJump;

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
    private Map<ArenaStage, ScoreboardHolder> scoreboard = Collections.emptyMap(); // DONE

    @Expose
    private int quitItemSlot = -1; // DONE

    @Expose
    private ItemHolder quitItem; // DONE

    public boolean isEnabled() {
        return enabled;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getChatPrefix() {
        return chatPrefix;
    }

    public boolean isPreventItemDropping() {
        return preventItemDropping;
    }

    public List<PotionEffect> getGivePotionEffects() {
        return givePotionEffects;
    }

    public DataHolder getDoubleJumpSettings() {
        return doubleJump;
    }

    public Map<Integer, ItemHolder> getItemsToAdd() {
        return itemsToAdd;
    }

    public Map<ArmorSlots, ItemHolder> getArmorToAdd() {
        return armorToAdd;
    }

    public Map<GameEvent, ExtensionTitle> getGameTitles() {
        return gameTitles;
    }

    public Map<Integer, Map<SenderType, List<String>>> getRunCommandsForFFAWinners() {
        return runCommandsForFFAWinners;
    }

    public Map<Integer, Map<SenderType, List<String>>> getRunCommandsForTeamWinners() {
        return runCommandsForTeamWinners;
    }

    public List<String> getSigns() {
        return signs;
    }

    public GameMode getWaitingMode() {
        return waitingMode;
    }

    public GameMode getInGameMode() {
        return ingameMode;
    }

    public List<DamageCause> getCancelledDamage() {
        return cancelledDamageInGame;
    }

    public List<DamageCause> getCancelledDamageInWaiting() {
        return cancelledDamageInWaiting;
    }

    public List<String> getExtensionCommands() {
        return extensionCommands;
    }

    public List<String> getAllowedCommands() {
        return allowedCommands;
    }

    public Map<ArenaStage, ScoreboardHolder> getScoreboard() {
        return scoreboard;
    }

    public int getQuitItemSlot() {
        return quitItemSlot;
    }

    public ItemHolder getQuitItem() {
        return quitItem;
    }

    public boolean isGiveDroppedItems() {
        return giveDroppedItems;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameExtension that = (GameExtension) o;
        return preventItemDropping == that.preventItemDropping &&
                giveDroppedItems == that.giveDroppedItems &&
                quitItemSlot == that.quitItemSlot &&
                Objects.equals(key, that.key) &&
                Objects.equals(displayName, that.displayName) &&
                Objects.equals(chatPrefix, that.chatPrefix) &&
                Objects.equals(runCommandsForFFAWinners, that.runCommandsForFFAWinners) &&
                Objects.equals(runCommandsForTeamWinners, that.runCommandsForTeamWinners) &&
                Objects.equals(givePotionEffects, that.givePotionEffects) &&
                Objects.equals(doubleJump, that.doubleJump) &&
                Objects.equals(itemsToAdd, that.itemsToAdd) &&
                Objects.equals(gameTitles, that.gameTitles) &&
                Objects.equals(signs, that.signs) &&
                waitingMode == that.waitingMode &&
                ingameMode == that.ingameMode &&
                Objects.equals(cancelledDamageInWaiting, that.cancelledDamageInWaiting) &&
                Objects.equals(cancelledDamageInGame, that.cancelledDamageInGame) &&
                Objects.equals(extensionCommands, that.extensionCommands) &&
                Objects.equals(allowedCommands, that.allowedCommands) &&
                Objects.equals(scoreboard, that.scoreboard) &&
                Objects.equals(quitItem, that.quitItem);
    }

    @Override
    public String toString() {
        return "GameExtension{" +
                "enabled=" + enabled +
                ", key='" + key + '\'' +
                ", chatPrefix='" + chatPrefix + '\'' +
                ", runCommandsForFFAWinners=" + runCommandsForFFAWinners +
                ", runCommandsForTeamWinners=" + runCommandsForTeamWinners +
                ", preventItemDropping=" + preventItemDropping +
                ", giveDroppedItems=" + giveDroppedItems +
                ", givePotionEffects=" + givePotionEffects +
                ", doubleJump=" + doubleJump +
                ", itemsToAdd=" + itemsToAdd +
                ", gameTitles=" + gameTitles +
                ", signs=" + signs +
                ", waitingMode=" + waitingMode +
                ", ingameMode=" + ingameMode +
                ", cancelledDamageInWaiting=" + cancelledDamageInWaiting +
                ", cancelledDamageInGame=" + cancelledDamageInGame +
                ", extensionCommands=" + extensionCommands +
                ", allowedCommands=" + allowedCommands +
                ", scoreboard=" + scoreboard +
                ", quitItemSlot=" + quitItemSlot +
                ", quitItem=" + quitItem +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, key, displayName, chatPrefix, runCommandsForFFAWinners, runCommandsForTeamWinners, preventItemDropping, giveDroppedItems, givePotionEffects, doubleJump, itemsToAdd, gameTitles, signs, waitingMode, ingameMode, cancelledDamageInWaiting, cancelledDamageInGame, extensionCommands, allowedCommands, scoreboard, quitItemSlot, quitItem);
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
            @Override public void run(Player player, String command) {
                ArenaPlayer p = ArenaPlayer.adapt(player);
                command = ScoreboardHolder.replacePlaceholders(p, command, p.getCurrentArena(), Collections.emptyMap());
                player.performCommand(command.replace("{winner}", player.getName()).replace("{player}", player.getName()));
            }
        },
        CONSOLE {
            @Override public void run(Player player, String command) {
                ArenaPlayer p = ArenaPlayer.adapt(player);
                command = ScoreboardHolder.replacePlaceholders(p, command, p.getCurrentArena(), Collections.emptyMap());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{winner}", player.getName())
                        .replace("{player}", player.getName()));
            }
        };

        public abstract void run(Player player, String command);

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

}
