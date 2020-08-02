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

import com.google.common.collect.ImmutableSet;
import io.github.spleefx.SpleefX;
import io.github.spleefx.data.StorageType;
import io.github.spleefx.dep.classloader.IsolatedClassLoader;
import io.github.spleefx.dep.relocation.Relocation;
import io.github.spleefx.dep.relocation.RelocationHandler;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Loads and manages runtime dependencies for the plugin.
 */
public class DependencyManager {

    protected static final String OKIO_STRING = String.valueOf(new char[]{'o', 'k', 'i', 'o'});
    protected static final String OKHTTP3_STRING = String.valueOf(new char[]{'o', 'k', 'h', 't', 't', 'p', '3'});

    /**
     * The plugin instance
     */
    private final SpleefX plugin;

    /**
     * A registry containing plugin specific behaviour for dependencies.
     */
    private final DependencyRegistry registry;

    /**
     * The path where library jars are cached.
     */
    private final Path cacheDirectory;

    /**
     * A map of dependencies which have already been loaded.
     */
    private final EnumMap<Dependency, Path> loaded = new EnumMap<>(Dependency.class);

    /**
     * A map of isolated classloaders which have been created.
     */
    private final Map<ImmutableSet<Dependency>, IsolatedClassLoader> loaders = new HashMap<>();

    /**
     * Cached relocation handler instance.
     */
    private final RelocationHandler relocationHandler;

    public DependencyManager(SpleefX plugin) {
        this.plugin = plugin;
        registry = new DependencyRegistry();
        cacheDirectory = setupCacheDirectory(plugin);
        relocationHandler = new RelocationHandler(this);
    }

    public IsolatedClassLoader obtainClassLoaderWith(Set<Dependency> dependencies) {
        ImmutableSet<Dependency> set = ImmutableSet.copyOf(dependencies);

        for (Dependency dependency : dependencies) {
            if (!loaded.containsKey(dependency)) {
                throw new IllegalStateException("Dependency " + dependency + " is not loaded.");
            }
        }

        synchronized (loaders) {
            IsolatedClassLoader classLoader = loaders.get(set);
            if (classLoader != null) {
                return classLoader;
            }

            URL[] urls = set.stream()
                    .map(loaded::get)
                    .map(file -> {
                        try {
                            return file.toUri().toURL();
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray(URL[]::new);

            classLoader = new IsolatedClassLoader(urls);
            loaders.put(set, classLoader);
            return classLoader;
        }
    }

    public void loadStorageDependencies(StorageType type) {
        loadDependencies(registry.resolveStorageDependencies(type));
    }

    public void loadDependencies(Set<Dependency> dependencies) {
        for (Dependency dependency : dependencies) {
            try {
                loadDependency(dependency);
            } catch (Throwable e) {
                plugin.getLogger().severe("Unable to load dependency " + dependency.name() + ".");
                e.printStackTrace();
            }
        }
    }

    private void loadDependency(Dependency dependency) throws Exception {
        if (loaded.containsKey(dependency)) {
            return;
        }
        Path file = remapDependency(dependency, downloadDependency(dependency));

        loaded.put(dependency, file);

        plugin.getReflectionClassLoader().addJarToClasspath(file);
    }

    private Path downloadDependency(Dependency dependency) throws DependencyDownloadException {
        Path file = cacheDirectory.resolve(dependency.getFileName() + ".jar");

        // if the file already exists, don't attempt to re-download it.
        if (Files.exists(file)) {
            return file;
        }

        DependencyDownloadException lastError = null;

        // attempt to download the dependency from each repo in order.
        for (DependencyRepository repo : DependencyRepository.values()) {
            try {
                repo.download(dependency, file);
                return file;
            } catch (DependencyDownloadException e) {
                lastError = e;
            }
        }
        System.out.println("Downloaded dependency " + dependency);
        throw Objects.requireNonNull(lastError);
    }

    private Path remapDependency(Dependency dependency, Path normalFile) throws Exception {
        List<Relocation> rules = new ArrayList<>(dependency.getRelocations());

        if (rules.isEmpty()) {
            return normalFile;
        }

        Path remappedFile = cacheDirectory.resolve(dependency.getFileName() + "-remapped.jar");

        // if the remapped source exists already, just use that.
        if (Files.exists(remappedFile)) {
            return remappedFile;
        }

        relocationHandler.remap(normalFile, remappedFile, rules);
        return remappedFile;
    }

    private static Path setupCacheDirectory(SpleefX plugin) {
        return plugin.getFileManager().createDirectory("libs").toPath();
    }

}
