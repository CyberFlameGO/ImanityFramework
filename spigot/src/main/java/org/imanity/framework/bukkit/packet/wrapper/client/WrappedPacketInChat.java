/*
 * MIT License
 *
 * Copyright (c) 2020 retrooper
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
import org.imanity.framework.bukkit.packet.PacketDirection;
import org.imanity.framework.bukkit.packet.type.PacketType;
import org.imanity.framework.bukkit.packet.wrapper.WrappedPacket;
import org.imanity.framework.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;

@Getter
@AutowiredWrappedPacket(value = PacketType.Client.CHAT, direction = PacketDirection.READ)
public final class WrappedPacketInChat extends WrappedPacket {

    private String message;

    public WrappedPacketInChat(Object packet) {
        super(packet);
    }

    @Override
    protected void setup() {
        this.message = readString(0);
    }

}
