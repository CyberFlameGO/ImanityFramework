/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
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

package org.imanity.framework.bukkit.reflection.version.protocol;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.impl.annotation.ProviderTestImpl;
import org.imanity.framework.bukkit.impl.test.ImplementationTest;
import org.imanity.framework.bukkit.reflection.MinecraftReflection;
import org.imanity.framework.bukkit.reflection.annotation.ProtocolImpl;
import org.imanity.framework.bukkit.reflection.resolver.FieldResolver;
import org.imanity.framework.bukkit.reflection.resolver.MethodResolver;
import org.imanity.framework.bukkit.reflection.resolver.ResolverQuery;
import org.imanity.framework.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import org.imanity.framework.bukkit.reflection.wrapper.FieldWrapper;
import org.imanity.framework.bukkit.reflection.wrapper.MethodWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@ProtocolImpl
@ProviderTestImpl(ProtocolCheckMethodVersion.TestImpl.class)
public class ProtocolCheckMethodVersion implements ProtocolCheck {

    private final FieldWrapper PLAYER_CONNECTION_FIELD;
    private final FieldWrapper NETWORK_MANAGER_FIELD;
    private final MethodWrapper GET_VERSION_METHOD;

    public ProtocolCheckMethodVersion() {
        NMSClassResolver nmsClassResolver = new NMSClassResolver();
        try {
            Class<?> networkManager = nmsClassResolver.resolve("NetworkManager");
            Class<?> playerConnection = nmsClassResolver.resolve("PlayerConnection");
            Class<?> entityPlayer = nmsClassResolver.resolve("EntityPlayer");

            FieldResolver fieldResolver = new FieldResolver(entityPlayer);

            PLAYER_CONNECTION_FIELD = fieldResolver.resolveByFirstTypeWrapper(playerConnection);

            fieldResolver = new FieldResolver(playerConnection);

            NETWORK_MANAGER_FIELD = fieldResolver.resolveByFirstTypeWrapper(networkManager);

            MethodResolver resolver = new MethodResolver(networkManager);
            GET_VERSION_METHOD = resolver.resolveWrapper(new ResolverQuery("getVersion", new Class[0]));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getVersion(Player player) {
        Object entityPlayer = MinecraftReflection.getHandleSilent(player);
        Object playerConnection = PLAYER_CONNECTION_FIELD.get(entityPlayer);
        Object networkManager = NETWORK_MANAGER_FIELD.get(playerConnection);

        return (int) GET_VERSION_METHOD.invoke(networkManager);
    }

    public static class TestImpl implements ImplementationTest {

        @Override
        public boolean test() {
            Class<?> networkManager;


            try {
                NMSClassResolver classResolver = new NMSClassResolver();
                networkManager = classResolver.resolve("NetworkManager");
            } catch (Throwable throwable) {
                throw new IllegalArgumentException(throwable);
            }

            try {

                MethodResolver resolver = new MethodResolver(networkManager);

                Method method = resolver.resolve(new ResolverQuery("getVersion", new Class[0]));

                return method != null && (method.getReturnType() == int.class || method.getReturnType() == Integer.class);
            } catch (Throwable throwable) {
            }

            try {
                FieldResolver resolver = new FieldResolver(networkManager);
                Field field = resolver.resolve(new ResolverQuery("version", Integer.class, int.class));

                return field != null;
            } catch (Throwable throwable) {
            }

            return false;
        }
    }
}
