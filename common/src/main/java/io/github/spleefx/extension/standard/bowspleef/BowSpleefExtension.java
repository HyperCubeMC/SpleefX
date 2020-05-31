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
package io.github.spleefx.extension.standard.bowspleef;

import com.google.gson.annotations.Expose;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.extension.ability.TripleArrowsAbility;
import io.github.spleefx.extension.ability.TripleArrowsAbility.Settings;

public class BowSpleefExtension extends GameExtension {

    public static final BowSpleefExtension EXTENSION =
            ExtensionsManager.getExtension("bow_spleef", ExtensionType.STANDARD, BowSpleefExtension.class);

    @Expose
    private boolean bounceArrows;

    @Expose
    private boolean removeTNTWhenPrimed;

    @Expose
    private TripleArrowsAbility.Settings tripleArrows = new Settings();

    public boolean getBounceArrows() {
        return bounceArrows;
    }

    public boolean getRemoveTNTWhenPrimed() {
        return removeTNTWhenPrimed;
    }

    public Settings getTripleArrows() {
        return tripleArrows;
    }
}