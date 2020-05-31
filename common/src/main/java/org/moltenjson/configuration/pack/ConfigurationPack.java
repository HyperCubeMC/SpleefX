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
package org.moltenjson.configuration.pack;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.moltenjson.json.JsonFile;
import org.moltenjson.json.JsonReader;
import org.moltenjson.json.JsonWriter;
import org.moltenjson.json.Refreshable;
import org.moltenjson.utils.Gsons;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a configuration pack. A configuration pack is similar in its aspect to
 * a {@link org.moltenjson.configuration.select.SelectableConfiguration}, however it provides better access
 * for non-{@code static} fields.
 * <p>
 * With a configuration pack, it is possible to associate a specific field with a file, and this field will get its value
 * from the file and will have its value saved to that same file when requested.
 * <p>
 * Fields should be annotated with {@link DeriveFrom} to get their values updated and saved.
 *
 * @param <I> The instance in which all fields are accessed from.
 * @see DeriveFrom
 */
public class ConfigurationPack<I> implements Refreshable<ConfigurationPack<I>> {

    /**
     * Represents the directory in which the files are in
     */
    private File directory;

    /**
     * Represents the GSON profile which does all the serializing and deserializing.
     */
    private Gson gson;

    /**
     * The main instance in which all fields are accessed from
     */
    private I instance;

    /**
     * The map which maps all fields with their files
     */
    private Map<Field, JsonFile> fieldMap = new HashMap<>();

    /**
     * Creates a new configuration pack
     *
     * @param instance  The instance in which all fields are accessed from.
     * @param directory The files directory
     * @param gson      The GSON profile to use
     */
    public ConfigurationPack(I instance, File directory, Gson gson) {
        this.instance = instance;
        this.directory = directory;
        this.gson = gson;
    }

    /**
     * Creates a new configuration pack
     *
     * @param instance  The instance in which all fields are accessed from.
     * @param directory The files directory
     */
    public ConfigurationPack(I instance, File directory) {
        this(instance, directory, Gsons.PRETTY_PRINTING);
    }

    /**
     * Gets the file of the field and adds it to the fields map
     *
     * @param field The field to register
     * @return The field file.
     */
    private JsonFile getFieldFile(Field field) {
        Preconditions.checkArgument(field.isAnnotationPresent(DeriveFrom.class), "Field " + field.getName() + " is not annotated with @DeriveFrom!");
        DeriveFrom derive = field.getAnnotation(DeriveFrom.class);
        return JsonFile.of(directory, derive.value().replace("/", File.separator));
    }

    /**
     * Updates the field by getting its value from the file. This is equivalent to refreshing the field value.
     *
     * @param field Field to update
     * @throws IOException Any IO exception when reading
     */
    public void updateField(Field field) throws IOException {
        JsonFile file = fieldMap.computeIfAbsent(field, this::getFieldFile);
        field.setAccessible(true);
        JsonReader reader = new JsonReader(file);
        JsonElement content = reader.getJsonElement();
        if ((!content.isJsonObject() || !content.getAsJsonObject().entrySet().isEmpty()) && (!content.isJsonArray() || content.getAsJsonArray().size() != 0)) {
            try {
                field.set(instance, gson.fromJson(content, field.getGenericType()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Attempted to set non-static field " + field.getName() + " from a class instance.");
            }
        }
    }

    /**
     * Updates the specified field name. This is equivalent to refreshing the field value.
     *
     * @param fieldName Field name to update
     * @throws IOException Any I/O exceptions while reading
     */
    public void updateField(String fieldName) throws IOException {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            updateField(field);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Saves the specified field to its corresponding file
     *
     * @param field Field to save
     * @throws IOException Any I/O exception while writing
     */
    public void saveField(Field field) throws IOException {
        JsonFile file = fieldMap.computeIfAbsent(field, this::getFieldFile);
        field.setAccessible(true);
        JsonWriter writer = new JsonWriter(file);
        try {
            writer.writeAndOverride(gson.toJsonTree(field.get(instance)), gson);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the specified field
     *
     * @param fieldName Field name to save
     * @throws IOException Any I/O exceptions while writing
     */
    public void saveField(String fieldName) throws IOException {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            saveField(field);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Registers all fields to their value from the file
     *
     * @return This object instance
     * @throws IOException Any I/O exceptions while reading
     */
    public ConfigurationPack<I> register() throws IOException {
        for (Field field : selectFields()) {
            updateField(field);
        }
        return this;
    }

    /**
     * Saves all the fields to their corresponding files
     *
     * @throws IOException Any I/O exception while writing
     */
    public void save() throws IOException {
        for (Field field : selectFields()) {
            saveField(field);
        }
    }

    /**
     * Updates the cached content with with whatever is found in the files.
     *
     * @return This object instance
     */
    @Override
    public ConfigurationPack<I> refresh() {
        for (Field field : selectFields()) {
            try {
                updateField(field);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    private List<Field> selectFields() {
        if (instance instanceof Class)
            return Arrays.stream(((Class<?>) instance).getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(DeriveFrom.class)).collect(Collectors.toList());
        return Arrays.stream(instance.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(DeriveFrom.class)).collect(Collectors.toList());
    }

}