/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package io.github.spleefx.dep;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import io.github.spleefx.data.StorageType;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Applies LuckPerms specific behaviour for {@link Dependency}s.
 */
public class DependencyRegistry {

    public static final ListMultimap<StorageType, Dependency> STORAGE_DEPENDENCIES = ImmutableListMultimap.<StorageType, Dependency>builder()
            .putAll(StorageType.MONGODB, Dependency.MONGODB_DRIVER)
            .putAll(StorageType.MYSQL, Dependency.MYSQL_DRIVER, Dependency.SLF4J_API, Dependency.SLF4J_SIMPLE)
            .putAll(StorageType.POSTGRESQL, Dependency.POSTGRESQL_DRIVER, Dependency.SLF4J_API, Dependency.SLF4J_SIMPLE)
            .putAll(StorageType.H2, Dependency.H2_DRIVER)
            .build();

    public Set<Dependency> resolveStorageDependencies(StorageType type) {
        Set<Dependency> dependencies = new LinkedHashSet<>(STORAGE_DEPENDENCIES.get(type));

        // don't load slf4j if it's already present
        if ((dependencies.contains(Dependency.SLF4J_API) || dependencies.contains(Dependency.SLF4J_SIMPLE)) && slf4jPresent()) {
            dependencies.remove(Dependency.SLF4J_API);
            dependencies.remove(Dependency.SLF4J_SIMPLE);
        }

        return dependencies;
    }

    public boolean shouldAutoLoad(Dependency dependency) {
        switch (dependency) {
            // all used within 'isolated' classloaders, and are therefore not
            // relocated.
            case ASM:
            case ASM_COMMONS:
            case JAR_RELOCATOR:
            case H2_DRIVER:
            case SQLITE_DRIVER:
                return true;
            default:
                return false;
        }
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean slf4jPresent() {
        return classExists("org.slf4j.Logger") && classExists("org.slf4j.LoggerFactory");
    }

}
