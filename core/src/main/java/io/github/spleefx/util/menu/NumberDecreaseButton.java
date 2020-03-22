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
package io.github.spleefx.util.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;

/**
 * Represents a button which decreases a numeric value
 */
public class NumberDecreaseButton extends Button {

    private Binder<Integer> value;

    /**
     * Creates a new number-decreasing button
     *
     * @param original     Original value
     * @param decreaseItem Item that decreases the value
     */
    public NumberDecreaseButton(int slot, Binder<Integer> original, ItemStack decreaseItem) {
        super(slot, decreaseItem);
        value = original;
    }

    public NumberDecreaseButton register(int minimum, NumberIncreaseButton increaseButton, ItemStack decreaseItem, BiConsumer<InventoryClickEvent, Integer> valueChange) {
        ItemStack increaseItem = increaseButton.getItem();
        increaseButton.getItem().setAmount(value.getValue() < 1 ? 1 : value.getValue());
        decreaseItem.setAmount(value.getValue() < 1 ? 1 : value.getValue());
        addAction(event -> {
            int decrease = event.isRightClick() ? 5 : 1;
            if (value.getValue() - decrease <= minimum) {
                event.getCurrentItem().setAmount(minimum);
                event.getInventory().getItem(increaseButton.getSlot()).setAmount(minimum);
                increaseItem.setAmount(minimum);
                value.setValue(minimum);
                valueChange.accept(event, minimum);
                return;
            }
            value.setValue(value.getValue() - decrease);
            valueChange.accept(event, value.getValue());
            event.getCurrentItem().setAmount(value.getValue() < 1 ? 1 : value.getValue());
            event.getInventory().getItem(increaseButton.getSlot()).setAmount(value.getValue() < 1 ? 1 : value.getValue());
        });
        addAction(CANCEL_ACTION);
        return this;
    }

    /**
     * Returns the value
     *
     * @return The value
     */
    public Binder<Integer> getValue() {
        return value;
    }
}