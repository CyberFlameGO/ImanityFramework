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

package org.imanity.framework.bukkit.packet.wrapper.client;

import lombok.Getter;
import lombok.SneakyThrows;
import org.imanity.framework.bukkit.packet.PacketDirection;
import org.imanity.framework.bukkit.packet.type.PacketType;
import org.imanity.framework.bukkit.packet.type.PacketTypeClasses;
import org.imanity.framework.bukkit.packet.wrapper.WrappedPacket;
import org.imanity.framework.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import org.imanity.framework.bukkit.reflection.resolver.FieldResolver;
import org.imanity.framework.bukkit.reflection.wrapper.FieldWrapper;

import java.lang.reflect.Field;

@AutowiredWrappedPacket(value = PacketType.Client.CUSTOM_PAYLOAD, direction = PacketDirection.READ)
@Getter
public final class WrappedPacketInCustomPayload extends WrappedPacket {
    private static Class<?> packetClass, nmsMinecraftKey, nmsPacketDataSerializer;

    private static boolean strPresentInIndex0;
    private String data;
    private Object minecraftKey, dataSerializer;
    public WrappedPacketInCustomPayload(Object packet) {
        super(packet);
    }

    public static void init() {
        packetClass = PacketTypeClasses.Client.CUSTOM_PAYLOAD;
        strPresentInIndex0 = new FieldResolver(packetClass)
            .resolveSilent(String.class, 0)
            .exists();
        try {
            nmsPacketDataSerializer = NMS_CLASS_RESOLVER.resolve("PacketDataSerializer");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            //Only on 1.13+
            nmsMinecraftKey = NMS_CLASS_RESOLVER.resolve("MinecraftKey");
        } catch (ClassNotFoundException e) {
            //Its okay, this means they are on versions 1.7.10 - 1.12.2
        }
    }

    @SneakyThrows
    @Override
    public void setup() {
        if (!strPresentInIndex0) {
            this.minecraftKey = readObject(0, nmsMinecraftKey);
            this.dataSerializer = readObject(0, nmsPacketDataSerializer);

        } else {
            this.data = readString(0);

            FieldWrapper<?> field = this.packet.getFieldByIndex(nmsPacketDataSerializer, 0);
            if (field != null) {
                this.dataSerializer = field.get(this.packet.getPacket());
            }
        }
    }

}
