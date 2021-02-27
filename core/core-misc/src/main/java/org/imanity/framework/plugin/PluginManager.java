/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.imanity.framework.plugin;

import com.google.common.collect.Sets;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PluginManager {

    public static PluginManager INSTANCE;

    public static boolean isInitialized() {
        return INSTANCE != null;
    }

    public static void initialize(PluginHandler pluginHandler) {
        if (INSTANCE != null) {
            throw new IllegalArgumentException("Don't Initialize twice!");
        }

        INSTANCE = new PluginManager(pluginHandler);
    }

    private final Map<String, AbstractPlugin> plugins;
    private final Set<PluginListenerAdapter> listenerAdapters;
    private final PluginHandler pluginHandler;

    public PluginManager(PluginHandler pluginHandler) {
        this.pluginHandler = pluginHandler;

        this.plugins = new ConcurrentHashMap<>();
        this.listenerAdapters = new TreeSet<>(Collections.reverseOrder(Comparator.comparingInt(PluginListenerAdapter::priority)));
    }

    public Collection<ClassLoader> getClassLoaders() {
        return this.plugins.values()
                .stream()
                .map(AbstractPlugin::getPluginClassLoader)
                .collect(Collectors.toList());
    }

    public void onPluginInitial(AbstractPlugin plugin) {
        synchronized (this.listenerAdapters) {
            this.listenerAdapters.forEach(listenerAdapter -> listenerAdapter.onPluginInitial(plugin));
        }
    }

    public void onPluginEnable(AbstractPlugin plugin) {
        synchronized (this.listenerAdapters) {
            this.listenerAdapters.forEach(listenerAdapter -> listenerAdapter.onPluginEnable(plugin));
        }
    }

    public void onPluginDisable(AbstractPlugin plugin) {
        synchronized (this.listenerAdapters) {
            this.listenerAdapters.forEach(listenerAdapter -> listenerAdapter.onPluginDisable(plugin));
        }
    }

    public Collection<AbstractPlugin> getPlugins() {
        return this.plugins.values();
    }

    public AbstractPlugin getPlugin(String name) {
        return this.plugins.get(name);
    }

    public void addPlugin(AbstractPlugin plugin) {
        this.plugins.put(plugin.getName(), plugin);
    }

    public void callFrameworkFullyDisable() {
        this.plugins.values().forEach(AbstractPlugin::onFrameworkFullyDisable);
    }

    public void registerListener(PluginListenerAdapter listenerAdapter) {
        synchronized (this.listenerAdapters) {
            this.listenerAdapters.add(listenerAdapter);
        }
    }

    @Nullable
    public AbstractPlugin getPluginByClass(Class<?> type) {
        String name = this.pluginHandler.getPluginByClass(type);
        if (name == null) {
            return null;
        }

        return this.getPlugin(name);
    }

}
