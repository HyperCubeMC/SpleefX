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
package io.github.spleefx.gui;

import io.github.spleefx.arena.api.ArenaType;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.arena.spleef.SpleefArena;
import io.github.spleefx.extension.standard.spleef.SpleefExtension;
import io.github.spleefx.team.GameTeam;
import io.github.spleefx.team.TeamColor;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.game.Metas;
import io.github.spleefx.util.item.ItemFactory;
import io.github.spleefx.util.item.Items;
import io.github.spleefx.util.menu.*;
import org.bukkit.DyeColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static io.github.spleefx.compatibility.CompatibilityHandler.getMaterialCompatibility;

public class ArenaSettingsGUI extends GameMenu {

    private static final BiConsumer<InventoryClickEvent, GameArena> RENAME = (event, arena) -> {
        Metas.set(event.getWhoClicked(), "spleefx.renaming", arena);
        Chat.plugin(event.getWhoClicked(), "&eType in the display name for arena &d" + arena.getKey() + "&e.");
        Chat.plugin(event.getWhoClicked(), "&eTo cancel, type &dcancel-edit&e.");
        event.getWhoClicked().closeInventory();
    };

    private static final BiConsumer<InventoryClickEvent, GameArena> DELETE = (event, arena) -> {
        GameArena.ARENAS.get().remove(arena.getKey());
        event.setCancelled(true);
        event.getWhoClicked().closeInventory();
    };

    private static final int ROWS = 6;

    /**
     * Creates a new menu
     *
     * @param arena Arena to edit settings for
     */
    public ArenaSettingsGUI(GameArena arena, HumanEntity sender) {
        super("&1Settings for " + arena.getDisplayName(), ROWS);
        cancelAllClicks = true;

        createNumberButtons(1, arena.getDeathLevel(), 27, 45, arena::setDeathLevel, new Button(36, Items.DEATH_LEVEL));

        if (arena.getArenaType() == ArenaType.FREE_FOR_ALL) {
            createNumberButtons(2, arena.getMaxPlayerCount(), 28, 46, arena::setMaxPlayerCount, new Button(37, Items.MAX_COUNT));
        } else {
            createNumberButtons(1, arena.getMembersPerTeam(), 28, 46, arena::setMembersPerTeam, new Button(37, Items.MEMBERS_PER_TEAM));
        }

        createNumberButtons(1, arena.getGameTime(), 29, 47, arena::setGameTime, new Button(38, Items.GAME_TIME));

        createNumberButtons(2, arena.getMinimum(), 30, 48, arena::setMinimum, new Button(39, Items.MINIMUM));

        createBooleanButtons(arena.getDropMinedBlocks(), 42, arena::setDropMinedBlocks, new Button(33, Items.DROP_MINED_BLOCKS));
        if (arena.getExtension() instanceof SpleefExtension)
            createBooleanButtons(((SpleefArena) arena).isMelt(), 43, ((SpleefArena) arena)::setMelt, new Button(34, Items.MELTING));

        createNumberButtons(1, arena.getBet(), 31, 49, arena::setBet, new Button(40, Items.BET_INT));
        createBooleanButtons(arena.shouldTakeBets(), 44, arena::setTakeBets, new Button(35, Items.BET_BOOL));


        /* Action buttons */
        setButton(new Button(16, Items.RENAME_ARENA).addAction((e) -> RENAME.accept(e, arena)));
        setButton(new Button(53, Items.DELETE).addAction((e) -> DELETE.accept(e, arena)));

        /* Number controlling buttons */
        if (arena.getArenaType() == ArenaType.FREE_FOR_ALL) {
            IntStream.range(0, 6).forEach(i -> setButton(new Button(i, Items.BARRIER)));
            IntStream.range(9, 15).forEach(i -> setButton(new Button(i, Items.BARRIER)));
        } else {
            for (int i = 2; i < TeamColor.values.length; i++) {
                TeamColor team = TeamColor.values[i];
                createBooleanButtons(arena.getTeams().contains(team),
                        i - 2 + 9, (n) -> team(n, arena, team), new Button(i - 2, team.getGuiItem()));
            }
        }
        Inventory inventory = createInventory();

        sender.openInventory(inventory);
    }

    private void createNumberButtons(int minimum, int orig, int slotInc, int slotDec, Consumer<Integer> valueChange, Button itemButton) {
        Binder<Integer> binder = new Binder<>(orig);
        ItemStack incItem = ItemFactory.create(getMaterialCompatibility().pane(DyeColor.LIME))
                .setName("&aIncrease")
                .setLore("", "&eLeft click &7-> &a+1", "&eRight click &7-> &a+5")
                .create();
        ItemStack decItem = ItemFactory.create(getMaterialCompatibility().pane(DyeColor.RED))
                .setLore("", "&eLeft click &7-> &c-1", "&eRight click &7-> &c-5")
                .setName("&cDecrease").create();
        NumberIncreaseButton increase = new NumberIncreaseButton(slotInc, binder, incItem);
        NumberDecreaseButton decrease = new NumberDecreaseButton(slotDec, binder, decItem);
        increase.register(incItem, decrease, (e, v) -> {
            valueChange.accept(v);
            e.getClickedInventory().setItem(itemButton.getSlot(), ItemFactory.create(itemButton.getItem().clone())
                    .addLoreLine("&bCurrent value: &d" + binder.getValue(), itemButton.getItem().getItemMeta().getLore().size() - 1).create());
        });
        decrease.register(minimum, increase, decItem, (e, v) -> {
            valueChange.accept(v);
            e.getClickedInventory().setItem(itemButton.getSlot(), ItemFactory.create(itemButton.getItem().clone())
                    .addLoreLine("&bCurrent value: &d" + binder.getValue(), itemButton.getItem().getItemMeta().getLore().size() - 1).create());
        });
        itemButton.addAction(e -> Chat.plugin(e.getWhoClicked(), "&eCurrent value: &d" + binder.getValue()));
        itemButton.setItem(ItemFactory.create(itemButton.getItem().clone()).addLoreLine("")
                .addLoreLine("&bCurrent value: &d" + binder.getValue()).create());
        setButton(increase).setButton(decrease).setButton(itemButton);
    }

    private void createBooleanButtons(boolean original, int slot, Consumer<Boolean> valueChange, Button itemButton) {
        BooleanButton booleanButton = new BooleanButton(slot, original, (e, v) -> valueChange.accept(v), Items.ENABLE, Items.DISABLE);
        setButton(booleanButton).setButton(itemButton);
    }

    private void team(boolean add, GameArena arena, TeamColor color) {
        if (add) {
            arena.getTeams().add(color);
            arena.gameTeams.add(new GameTeam(color, new ArrayList<>()));
        } else {
            arena.getTeams().remove(color);
            arena.gameTeams.removeIf(gameTeam -> gameTeam.getColor() == color);
        }
    }
}
