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
package io.github.spleefx.economy.booster;

import io.github.spleefx.util.menu.Button;
import io.github.spleefx.util.menu.GameMenu;
import io.github.spleefx.util.message.message.Message;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;

import static io.github.spleefx.economy.booster.BoosterState.*;
import static io.github.spleefx.util.game.Chat.colorize;

public class BoosterMenu extends GameMenu {

    /**
     * Creates a new menu
     *
     * @param boosters The boosters to list
     */
    public BoosterMenu(List<BoosterInstance> boosters) {
        super(BoosterFactory.BOOSTER_MENU_TITLE.get(), getAppropriateSize(boosters.size()));
        cancelAllClicks = true;
        for (int i = 0; i < boosters.size(); i++) {
            BoosterInstance booster = boosters.get(i);
            switch (booster.getState()) {
                case ACTIVE:
                    setButton(new Button(i, applyPlaceholders(booster, booster.getType().getItems().get(ACTIVE).factory().create()))
                            .addAction(Button.CLOSE_INVENTORY)
                            .addAction(e -> {
                                if (BoosterFactory.CAN_BE_PAUSED.get()) {
                                    booster.pause();
                                    Message.BOOSTER_PAUSED.reply(e.getWhoClicked(), booster);
                                }
                            }));
                    break;
                case AVAILABLE:
                    setButton(new Button(i, applyPlaceholders(booster, booster.getType().getItems().get(AVAILABLE).factory().create()))
                            .addAction(Button.CLOSE_INVENTORY).addAction(p -> {
                                if (booster.activate(((Player) p.getWhoClicked())))
                                    Message.BOOSTER_ACTIVATED.reply(p.getWhoClicked(), booster);
                            }));
                    break;
                case PAUSED:
                    setButton(new Button(i, applyPlaceholders(booster, booster.getType().getItems().get(PAUSED).factory().create()))
                            .addAction(Button.CLOSE_INVENTORY).addAction(p -> {
                                if (booster.activate(((Player) p.getWhoClicked())))
                                    Message.BOOSTER_ACTIVATED.reply(p.getWhoClicked(), booster);
                            }));
            }
        }
    }

    private ItemStack applyPlaceholders(BoosterInstance booster, ItemStack item) {
        ItemMeta current = item.getItemMeta();
        current.setDisplayName(applyPlaceholders(booster, current.getDisplayName()));
        if (current.hasLore())
            current.setLore(current.getLore().stream().map(s -> applyPlaceholders(booster, s)).collect(Collectors.toList()));
        item.setItemMeta(current);
        return item;
    }

    private String applyPlaceholders(BoosterInstance booster, String value) {
        BoosterFactory type = booster.getType();
        return colorize(value
                .replace("{booster_type_displayname}", type.getDisplayName())
                .replace("{booster_type}", type.getDisplayName()) // fallback lol
                .replace("{booster_type_key}", type.getKey())
                .replace("{booster_multiplier}", Double.toString(booster.getMultiplier()))
                .replace("{booster_time_left}", Long.toString(booster.getDuration()))
                .replace("{booster_is_active}", booster.isActive() ? "&cActive" : "&aAvailable"));
    }

    private static final Map<IntPredicate, Integer> SLOT_SIZE = new HashMap<>();

    public static int getAppropriateSize(int size) {
        return SLOT_SIZE.entrySet().stream().filter(e -> e.getKey().test(size)).findFirst().map(Entry::getValue).orElse(6);
    }

    static {
        SLOT_SIZE.put((v) -> isBetween(0, 9, v), 1);
        SLOT_SIZE.put((v) -> isBetween(10, 18, v), 2);
        SLOT_SIZE.put((v) -> isBetween(19, 27, v), 3);
        SLOT_SIZE.put((v) -> isBetween(28, 36, v), 4);
        SLOT_SIZE.put((v) -> isBetween(37, 45, v), 5);
        SLOT_SIZE.put((v) -> isBetween(46, 54, v), 6);
        SLOT_SIZE.put((v) -> isBetween(55, 63, v), 7);
    }

    private static boolean isBetween(int a, int b, int test) {
        return test >= a && test <= b;
    }

}