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

import lombok.Getter;
import lombok.Setter;
import org.imanity.framework.ServiceDependencyType;
import org.imanity.framework.plugin.AbstractPlugin;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Getter
@Setter
public class SimpleBeanDetails implements BeanDetails {

    private Object instance;
    private String name;
    private Class<?> type;

    private AbstractPlugin plugin;
    private Set<String> children;

    public SimpleBeanDetails(Object instance, String name, Class<?> type) {
        this.instance = instance;
        this.name = name;
        this.type = type;
        this.children = new HashSet<>();
    }

    @Override
    public boolean shouldInitialize() throws InvocationTargetException, IllegalAccessException {
        return true;
    }

    @Override
    public void call(Class<? extends Annotation> annotation) throws InvocationTargetException, IllegalAccessException {
    }

    @Override
    public boolean isStage(GenericBeanDetails.ActivationStage stage) {
        return true;
    }

    @Override
    public boolean isActivated() {
        return true;
    }

    @Override
    public boolean isDestroyed() {
        return false;
    }

    @Override
    public @Nullable String getTag(String key) {
        return null;
    }

    @Override
    public boolean hasTag(String key) {
        return false;
    }

    @Override
    public void addTag(String key, String value) {

    }

    @Override
    public void setStage(GenericBeanDetails.ActivationStage stage) {

    }

    @Override
    public void setDisallowAnnotations(Map<Class<? extends Annotation>, String> disallowAnnotations) {

    }

    @Override
    public void setAnnotatedMethods(Map<Class<? extends Annotation>, Collection<Method>> annotatedMethods) {

    }

    @Override
    public void setTags(Map<String, String> tags) {

    }

    @Override
    public GenericBeanDetails.ActivationStage getStage() {
        return null;
    }

    @Override
    public Map<Class<? extends Annotation>, String> getDisallowAnnotations() {
        return null;
    }

    @Override
    public Map<Class<? extends Annotation>, Collection<Method>> getAnnotatedMethods() {
        return null;
    }

    @Override
    public Map<String, String> getTags() {
        return null;
    }

    @Override
    public void bindWith(AbstractPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public AbstractPlugin getBindPlugin() {
        return this.plugin;
    }

    @Override
    public boolean isBind() {
        return this.plugin != null;
    }

    @Override
    public boolean hasDependencies() {
        return false;
    }

    public Set<String> getChildren() {
        return Collections.unmodifiableSet(this.children);
    }

    @Override
    public void addChildren(String children) {
        this.children.add(children);
    }

    @Override
    public void removeChildren(String children) {
        this.children.remove(children);
    }

    @Override
    public List<String> getDependencies(ServiceDependencyType type) {
        return Collections.emptyList();
    }

    @Override
    public Set<Map.Entry<ServiceDependencyType, List<String>>> getDependencyEntries() {
        return Collections.emptySet();
    }
}
