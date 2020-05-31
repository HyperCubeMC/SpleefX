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

import com.google.gson.annotations.Expose;
import io.github.spleefx.compatibility.ProtocolNMS;
import org.apache.commons.lang.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a JSON text message.
 */
public class ComponentJSON {

    /**
     * A list which contains all JSON components
     */
    @Expose
    private List<ChatComponent> components = new LinkedList<>();

    /**
     * Appends a {@link ChatComponent} so it gets included in the parent message components
     *
     * @param component Component to append
     * @return A reference to this builder
     */
    public ComponentJSON append(ChatComponent component) {
        components.add(component);
        return this;
    }

    /**
     * Appends a space
     *
     * @return A reference to this builder
     */
    public ComponentJSON space() {
        return append(ChatComponent.SPACE);
    }

    public ComponentJSON clear() {
        components.clear();
        return this;
    }

    /**
     * Returns the JSON string representing this message
     *
     * @return The JSON string
     */
    @Override
    public String toString() {
        return ProtocolNMS.CHAT_GSON.toJson(components);
    }

    public String getStripped() {
        StringBuilder b = new StringBuilder();
        components.forEach(c -> b.append(c.getText()));
        return StringUtils.normalizeSpace(b.toString());
    }

}
