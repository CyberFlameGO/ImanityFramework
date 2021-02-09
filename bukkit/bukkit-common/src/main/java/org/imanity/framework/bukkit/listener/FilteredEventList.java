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

package org.imanity.framework.bukkit.listener;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerEvent;
import org.imanity.framework.bukkit.reflection.resolver.MethodResolver;
import org.imanity.framework.bukkit.reflection.wrapper.MethodWrapper;
import org.imanity.framework.reflect.Reflect;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Getter
public class FilteredEventList {

    private static final Map<Class<?>, Function<Event, Player>> EVENT_PLAYER_METHODS = new ConcurrentHashMap<>();
    private static final Set<Class<?>> NO_METHODS = Sets.newConcurrentHashSet();

    static {

        EVENT_PLAYER_METHODS.put(BlockBreakEvent.class, event -> ((BlockBreakEvent) event).getPlayer());
        EVENT_PLAYER_METHODS.put(BlockPlaceEvent.class, event -> ((BlockPlaceEvent) event).getPlayer());

    }

    public static void putCustomPlayerMethod(Class<?> eventClass, Function<Event, Player> method) {
        EVENT_PLAYER_METHODS.put(eventClass, method);
    }

    private final Predicate<Event>[] filters;

    private FilteredEventList(Builder builder) {
        this.filters = builder.filters.toArray(new Predicate[0]);
    }

    public boolean check(Event event) {

        for (Predicate<Event> filter : this.filters) {
            if (!filter.test(event)) {
                return false;
            }
        }

        return true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final List<Predicate<Event>> filters;

        public Builder() {
            this.filters = new ArrayList<>(1);
        }

        public Builder filter(Predicate<Event> filter) {
            this.filters.add(filter);
            return this;
        }

        public Builder filter(BiPredicate<Player, Event> filter) {
            this.filters.add(event -> {
                Player player = null;
                Class<?> type = event.getClass();
                if (event instanceof PlayerEvent) {
                    player = ((PlayerEvent) event).getPlayer();
                } else if (EVENT_PLAYER_METHODS.containsKey(type)) {
                    player = EVENT_PLAYER_METHODS.get(type).apply(event);
                } else {
                    MethodHandle methodHandle = null;

                    if (!NO_METHODS.contains(type)) {
                        for (Method method : Reflect.getDeclaredMethods(type)) {
                            if (method.getParameterCount() == 0) {
                                Class<?> returnType = method.getReturnType();
                                if (Player.class.isAssignableFrom(returnType)) {
                                    try {
                                        methodHandle = Reflect.lookup().unreflect(method);

                                        MethodHandleFunction methodHandleFunction = new MethodHandleFunction(methodHandle);
                                        EVENT_PLAYER_METHODS.put(event.getClass(), methodHandleFunction);

                                        player = methodHandleFunction.apply(event);
                                        break;
                                    } catch (Throwable throwable) {
                                        throw new IllegalArgumentException("Something wrong while looking for player", throwable);
                                    }
                                }
                            }
                        }

                        if (methodHandle == null) {
                            NO_METHODS.add(type);
                        }
                    }
                }

                if (player != null) {
                    return filter.test(player, event);
                }
                return true;
            });

            return this;
        }

        public FilteredEventList build() {
            return new FilteredEventList(this);
        }

    }

    @RequiredArgsConstructor
    private static class MethodHandleFunction implements Function<Event, Player> {

        private final MethodHandle methodHandle;

        @Override
        public Player apply(Event event) {
            try {
                return (Player) methodHandle.invoke(event);
            } catch (Throwable throwable) {
                throw new IllegalArgumentException("Something wrong while looking for player", throwable);
            }
        }
    }

}
