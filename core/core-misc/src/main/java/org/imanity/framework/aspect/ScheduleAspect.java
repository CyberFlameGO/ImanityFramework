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

package org.imanity.framework.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.imanity.framework.FrameworkMisc;
import org.imanity.framework.ScheduledAtFixedRate;

import java.lang.reflect.Method;

@Aspect
public class ScheduleAspect {

    @Around("execution(@org.imanity.framework.ScheduledAtFixedRate * * (..))")
    public Object schedule(ProceedingJoinPoint point) {
        final Class<?> returned = ((MethodSignature) point.getSignature()).getMethod().getReturnType();

        if (!returned.equals(Void.TYPE)) {
            throw new IllegalStateException(
                    String.format(
                            "%s: Return type is %s, not void, cannot use @ScheduledAtFixedRate",
                            point.toShortString(),
                            returned.getCanonicalName()
                    )
            );
        }

        final Method method = ((MethodSignature) point.getSignature()).getMethod();

        final ScheduledAtFixedRate annotation = method.getAnnotation(ScheduledAtFixedRate.class);
        Runnable runnable = () -> {
            try {
                point.proceed();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };

        if (annotation.async()) {
            FrameworkMisc.TASK_SCHEDULER.runAsyncRepeated(runnable, annotation.delay(), annotation.ticks());
        } else {
            FrameworkMisc.TASK_SCHEDULER.runRepeated(runnable, annotation.delay(), annotation.ticks());
        }

        return null;
    }

}
