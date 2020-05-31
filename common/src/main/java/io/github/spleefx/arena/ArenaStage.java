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
package io.github.spleefx.arena;

import io.github.spleefx.message.MessageKey;

/**
 * Represents a running arena stage
 */
public enum ArenaStage {

    /**
     * The arena is waiting for players to join
     */
    WAITING(MessageKey.WAITING, true),

    /**
     * The arena is in countdown
     */
    COUNTDOWN(MessageKey.COUNTDOWN, true),

    /**
     * The arena is running and is actiev
     */
    ACTIVE(MessageKey.ACTIVE, false),

    /**
     * The arena is regenerating
     */
    REGENERATING(MessageKey.REGENERATING, false),

    /**
     * The arena's setup is not finished
     */
    NEEDS_SETUP(MessageKey.NEEDS_SETUP, false),

    /**
     * The arena mode is disabled
     */
    DISABLED(MessageKey.DISABLED, false);

    /**
     * The text representing the state
     */
    private MessageKey key;

    /**
     * Whether is the arena playable in this arena state or not
     */
    private boolean playable;

    /**
     * Initiates a new arena stage
     *
     * @param key      The message key representing this state
     * @param playable Whether is the arena playable in this arena state or not
     */
    ArenaStage(MessageKey key, boolean playable) {
        this.key = key;
        this.playable = playable;
    }

    /**
     * Getter for property 'state'.
     *
     * @return Value for property 'state'.
     */
    public String getState() {
        return key.getText();
    }

    /**
     * Whether is the arena playable in this arena state or not
     *
     * @return Whether is the arena playable in this arena state or not
     */
    public boolean isPlayable() {
        return playable;
    }

}
