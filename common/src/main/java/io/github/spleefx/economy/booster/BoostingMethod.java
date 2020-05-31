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

/**
 * Represents boosting strategies for boosting when there is more than 1 booster
 */
public enum BoostingMethod {

    /**
     * Sum the multipliers of all active boosters
     */
    SUM_MULTIPLIERS,

    /**
     * Multiplies all the multipliers of the active booster
     */
    MULTIPLICATIVE,

    /**
     * Use the highest booster multiplier only
     */
    USE_HIGHEST

}
