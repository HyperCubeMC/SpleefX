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
package io.github.spleefx.compatibility.chat;

@SuppressWarnings("FieldCanBeLocal")
public class ChatEvents {

    //<editor-fold desc="Chat wrappers" defaultstate="collapsed">

    /**
     * Represents a clickable chat event
     *
     * @see HoverEvent
     */
    public enum ClickEvent {

        /**
         * Prompts a URL when clicked
         */
        OPEN_URL("open_url"),

        /**
         * Opens a file when clicked
         */
        OPEN_FILE("open_file"),

        /**
         * Forces the user to run a command when clicked.
         * <p>
         * Note: Commands must be prefixed with "/" when inputted, otherwise the user will send a chat message
         * rather than a command.
         */
        RUN_COMMAND("run_command"),

        /**
         * Suggests a command in the player's chat field when clicked
         */
        SUGGEST_COMMAND("suggest_command");

        /**
         * Represents the protocol name for this event type
         */
        private final String protocolName;

        ClickEvent(String protocolName) {
            this.protocolName = protocolName;
        }

        /**
         * Returns the protocol name
         *
         * @return The protocol name
         */
        @Override
        public String toString() {
            return protocolName;
        }
    }

    /**
     * Represents a hoverable chat event
     *
     * @see ClickEvent
     */
    public enum HoverEvent {

        /**
         * Shows text when the component is being hovered on
         */
        SHOW_TEXT("show_text"),

        /**
         * Shows item information when the component is being hovered on
         */
        SHOW_ITEM("show_item"),

        /**
         * Shows an entity information when the component is being hovered on
         */
        SHOW_ENTITY("show_entity");

        /**
         * Represents the protocol name for this event type
         */
        private final String protocolName;

        HoverEvent(String protocolName) {
            this.protocolName = protocolName;
        }

        /**
         * Returns the protocol name
         *
         * @return The protocol name
         */
        @Override
        public String toString() {
            return protocolName;
        }
    }

    /**
     * An encapsulation for click actions; Gson purposes
     */
    public static class ClickAction {

        /**
         * The click action
         */
        public String action = "NONE";

        /**
         * The value
         */
        public String value;

        public ClickAction action(String action) {
            this.action = action;
            return this;
        }

        public ClickAction value(String value) {
            this.value = value;
            return this;
        }
    }

    /**
     * An encapsulation for hover events; Gson purposes
     */
    public static class HoverAction {

        /**
         * The hover action
         */
        public String action = "NONE";

        /**
         * The value
         */
        public String value;

        protected HoverAction action(String action) {
            this.action = action;
            return this;
        }

        protected HoverAction value(String value) {
            this.value = value;
            return this;
        }
    }
    //</editor-fold>

}
