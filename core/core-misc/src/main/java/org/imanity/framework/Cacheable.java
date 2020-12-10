package org.imanity.framework;

import org.intellij.lang.annotations.Language;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cacheable {

    /**
     * Lifetime of an object in cache, in time units.
     */
    int lifetime() default 1;

    /**
     * Time units of object lifetime.
     *
     * <p>The minimum unit you can use is a second. We simply can't cache for
     * less than a second, because cache is being cleaned every second.
     */
    TimeUnit unit() default TimeUnit.MINUTES;

    /**
     * Keep in cache forever.
     */
    boolean forever() default false;

    /**
     * Storing the key of the cacheable
     *
     */
    @Language("JavaScript") String key() default "";

    /**
     * Don't store if condition is false
     *
     */
    @Language("JavaScript") String condition() default "";

    /**
     * Prevent to store value if one of argument were null
     *
     */
    boolean preventArgumentNull() default true;

    /**
     * Identifies a method that should flush all cached entities of
     * this class/object, before being executed.
     * @since 0.7.18
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface ClearBefore {
    }

    /**
     * Identifies a method that should flush all cached entities of
     * this class/object, after being executed.
     * @since 0.7.18
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface ClearAfter {
    }

}
