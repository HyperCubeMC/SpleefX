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

/**
 * A simple value tracking object
 *
 * @param <T> Value to track
 */
public class Binder<T> {

    /**
     * The current value
     */
    private T value;

    /**
     * Creates a new value binder
     *
     * @param value Value to bind
     */
    public Binder(T value) {
        this.value = value;
    }

    /**
     * Returns the current value
     *
     * @return The current value
     */
    public T getValue() {
        return value;
    }

    /**
     * Updates the value
     *
     * @param value New value to set
     */
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * Returns the current value string
     *
     * @return The value string
     */
    @Override
    public String toString() {
        return value.toString();
    }

}
