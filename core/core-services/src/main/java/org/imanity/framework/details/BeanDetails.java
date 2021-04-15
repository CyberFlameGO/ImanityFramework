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

package org.imanity.framework.details;

import lombok.SneakyThrows;
import org.imanity.framework.ServiceDependencyType;
import org.imanity.framework.plugin.AbstractPlugin;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface BeanDetails {

    boolean shouldInitialize() throws InvocationTargetException, IllegalAccessException;

    void call(Class<? extends Annotation> annotation) throws InvocationTargetException, IllegalAccessException;

    boolean isStage(ActivationStage stage);

    boolean isActivated();

    boolean isDestroyed();

    @Nullable
    String getTag(String key);

    boolean hasTag(String key);

    void addTag(String key, String value);

    void setName(String name);

    void setStage(ActivationStage stage);

    void setDisallowAnnotations(java.util.Map<Class<? extends Annotation>, String> disallowAnnotations);

    void setAnnotatedMethods(java.util.Map<Class<? extends Annotation>, java.util.Collection<Method>> annotatedMethods);

    void setInstance(Object instance);

    void setType(Class<?> type);

    void setTags(java.util.Map<String, String> tags);

    String getName();

    ActivationStage getStage();

    Map<Class<? extends Annotation>, String> getDisallowAnnotations();

    Map<Class<? extends Annotation>, java.util.Collection<Method>> getAnnotatedMethods();

    @Nullable
    Object getInstance();

    Class<?> getType();

    Map<String, String> getTags();

    void bindWith(AbstractPlugin plugin);

    AbstractPlugin getBindPlugin();

    boolean isBind();

    boolean hasDependencies();

    Set<String> getChildren();

    void addChildren(String children);

    void removeChildren(String children);

    List<String> getDependencies(ServiceDependencyType type);

    Set<Map.Entry<ServiceDependencyType, List<String>>> getDependencyEntries();

    default Set<String> getAllDependencies() {
        return this.getDependencyEntries().stream()
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .collect(Collectors.toSet());
    }

    default void onEnable() {

    }

    default void onDisable() {

    }

    enum ActivationStage {

        NOT_LOADED,
        PRE_INIT_CALLED,
        POST_INIT_CALLED,

        PRE_DESTROY_CALLED,
        POST_DESTROY_CALLED

    }

}
