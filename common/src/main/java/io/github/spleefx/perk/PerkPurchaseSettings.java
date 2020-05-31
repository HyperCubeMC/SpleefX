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
package io.github.spleefx.perk;

import com.google.gson.annotations.Expose;

/**
 * Represents settings for a perk purchase
 */
public class PerkPurchaseSettings {

    @Expose
    private int price;

    @Expose
    private int ingameAmount;

    @Expose
    private int gamesUsableFor;

    public PerkPurchaseSettings(int price, int ingameAmount, int gamesUsableFor) {
        this.price = price;
        this.ingameAmount = ingameAmount;
        this.gamesUsableFor = gamesUsableFor;
    }

    public int getPrice() {
        return price;
    }

    public int getIngameAmount() {
        return ingameAmount;
    }

    public int getGamesUsableFor() {
        return gamesUsableFor;
    }
}
