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
 * Represents a button which controls a boolean state
 */
public class BooleanButton extends Button {

    /**
     * The current button state (enabled or disabled)
     */
    private boolean state;

    /**
     * Creates a new toggle button
     *
     * @param on      Whether is the button on or not
     * @param onItem  The item to toggle the button on
     * @param offItem The item to toggle the button off
     */
    public BooleanButton(int slot, boolean on, BiConsumer<InventoryClickEvent, Boolean> stateChange, ItemStack onItem, ItemStack offItem) {
        super(slot, on ? offItem : onItem);
        state = on;
        addAction(CANCEL_ACTION);
        addAction(event -> {
            state = !state;
            stateChange.accept(event, state);
            event.setCurrentItem(state ? offItem : onItem);
        });
    }

    /**
     * Returns the button state
     *
     * @return The button state
     */
    public boolean getState() {
        return state;
    }

}