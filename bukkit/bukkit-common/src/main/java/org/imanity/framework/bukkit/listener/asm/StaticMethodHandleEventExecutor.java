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

package org.imanity.framework.bukkit.listener.asm;

import com.google.common.base.Preconditions;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.imanity.framework.bukkit.listener.FilteredEventList;
import org.imanity.framework.reflect.Reflect;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class StaticMethodHandleEventExecutor implements EventExecutor {

    private final boolean ignoredFilters;
    private final FilteredEventList eventList;
    private final Class<? extends Event> eventClass;
    private final MethodHandle handle;

    public StaticMethodHandleEventExecutor(@NonNull Class<? extends Event> eventClass, @NonNull Method m, boolean ignoredFilters, FilteredEventList eventList) {
        Preconditions.checkArgument(Modifier.isStatic(m.getModifiers()), "Not a static method: %s", m);
        Preconditions.checkArgument(eventClass != null, "eventClass is null");
        this.eventClass = eventClass;
        this.ignoredFilters = ignoredFilters;
        this.eventList = eventList;
        try {
            m.setAccessible(true);
            this.handle = Reflect.lookup().unreflect(m);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Unable to set accessible", e);
        }
    }

    @Override
    @SneakyThrows
    public void execute(@NonNull Listener listener, @NonNull Event event) throws EventException {
        if (eventClass.isInstance(event) && (ignoredFilters || eventList.check(event))) {
            handle.invoke(event);
        }
    }
}
