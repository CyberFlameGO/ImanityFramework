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

package org.imanity.framework.bukkit.listener.timings;

import java.lang.reflect.Method;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.imanity.framework.Autowired;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.timings.MCTiming;
import org.imanity.framework.timings.TimingService;

public class TimedEventExecutor implements EventExecutor {

    @Autowired
    private static TimingService TIMING_SERVICE;

    private final EventExecutor executor;
    private final MCTiming timings;

    public TimedEventExecutor(EventExecutor eventExecutor, Plugin plugin, Method method, Class<? extends Event> eventClass) {
        this.executor = eventExecutor;
        if (method == null && eventExecutor.getClass().getEnclosingClass() != null) {
            method = eventExecutor.getClass().getEnclosingMethod();
        }

        String methodName;
        if (method != null) {
            methodName = method.getDeclaringClass().getName();
        } else {
            methodName = eventExecutor.getClass().getName();
        }

        String eventName = eventClass.getSimpleName();
        boolean special = "BlockPhysicsEvent".equals(eventName) || "Drain".equals(eventName) || "Fill".equals(eventName);
        this.timings = TIMING_SERVICE.of(plugin, (special ? "## " : "") + "Event: " + methodName + " (" + eventName + ")");
    }

    public void execute(Listener listener, Event event) throws EventException {
        if (!event.isAsynchronous() && Imanity.IMPLEMENTATION.isServerThread()) {
            try (MCTiming ignored = this.timings.startTiming()) {
                this.executor.execute(listener, event);
            }
        } else {
            this.executor.execute(listener, event);
        }
    }
}
