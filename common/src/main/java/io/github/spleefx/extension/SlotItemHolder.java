/*
 * * Copyright 2019-2020 github.com/ReflxctionDev
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

import com.google.gson.annotations.Expose;
import org.bukkit.inventory.InventoryHolder;

/**
 * Represents an {@link ItemHolder} with an inventory slot index.
 */
public class SlotItemHolder extends ItemHolder {

    @Expose
    private int slot;

    public int getSlot() {
        return slot;
    }

    public SlotItemHolder() {
    }

    public SlotItemHolder(int slot) {
        this.slot = slot;
    }

    public SlotItemHolder setSlot(int slot) {
        this.slot = slot;
        return this;
    }

    public void give(InventoryHolder inventoryHolder) {
        inventoryHolder.getInventory().setItem(slot, factory().create());
    }
}
