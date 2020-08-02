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

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.spleefx.compatibility.chat.ChatEvents.ClickAction;
import io.github.spleefx.compatibility.chat.ChatEvents.ClickEvent;
import io.github.spleefx.compatibility.chat.ChatEvents.HoverAction;
import io.github.spleefx.compatibility.chat.ChatEvents.HoverEvent;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.message.message.Message;
import org.moltenjson.json.JsonBuilder;

import java.lang.reflect.Type;

/**
 * A class for creating chat messages with clickable and hoverable events
 */
public class ChatComponent {

    /**
     * A component representing the space
     */
    public static final ChatComponent SPACE = new ChatComponent().setText(" ", false);

    /**
     * The component's text
     */
    private String text;

    /**
     * The component's click event
     */
    private final ClickAction clickAction = new ClickAction();

    /**
     * The component's hover event
     */
    private final HoverAction hoverAction = new HoverAction();

    public String getText() {
        return text;
    }

    /**
     * Sets the text
     *
     * @param text   New value to set
     * @param prefix Whether should the message be prefixed or not
     * @return This component
     */
    public ChatComponent setText(String text, boolean prefix) {
        this.text = (prefix ? Message.PREFIX.create() : "") + Chat.colorize(text);
        return this;
    }

    /**
     * Sets the hover action
     *
     * @param action New action to set
     * @return This component
     */
    public ChatComponent setHoverAction(HoverEvent action, String value) {
        hoverAction.action(action.toString()).value(Chat.colorize(value));
        return this;
    }

    /**
     * Sets the click action
     *
     * @param action New action to set
     * @return This component
     */
    public ChatComponent setClickAction(ClickEvent action, String value) {
        clickAction.action(action.toString()).value(value);
        return this;
    }

    public static class Adapter implements JsonSerializer<ChatComponent> {

        @Override
        public JsonElement serialize(ChatComponent component, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonBuilder builder = new JsonBuilder()
                    .map("text", component.text);
            if (!component.clickAction.action.equals("NONE"))
                builder.map("clickEvent", new JsonBuilder().map("action", component.clickAction.action).map("value", component.clickAction.value).buildJsonObject());
            if (!component.hoverAction.action.equals("NONE"))
                builder.map("hoverEvent", new JsonBuilder().map("action", component.hoverAction.action).map("value", component.hoverAction.value).buildJsonObject());
            return builder.buildJsonElement();
        }
    }
}
