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

package org.imanity.framework.bukkit.packet.event.type;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.packet.PacketDirection;
import org.imanity.framework.bukkit.packet.event.PacketEvent;
import org.imanity.framework.bukkit.packet.type.PacketType;
import org.imanity.framework.bukkit.packet.wrapper.WrappedPacket;

/**
 * This event is called each time the server sends a packet to the client.
 */
public final class PacketSendEvent extends PacketEvent {
    private final Player player;
    private final Object packet;
    private boolean cancelled;

    public PacketSendEvent(final Player player, final Object packet) {
        this.player = player;
        this.packet = packet;
        this.cancelled = false;
    }


    public Player getPlayer() {
        return this.player;
    }

    /**
     * Get the packet's name (NMS packet class simple name)
     * @deprecated It is recommended not to use this, as it is an expensive function to call.
     * @return Name of the packet
     */
    @Deprecated
    public String getPacketName() {
        return this.packet.getClass().getSimpleName();
    }

    /**
     * Get the ID of the packet
     *
     * @return packet id
     */
    public byte getPacketId() {
        return PacketType.Server.PACKET_IDS.getOrDefault(packet.getClass(), (byte) -1);
    }

    /**
     * Get the NMS packet object
     *
     * @return packet object
     */
    public Object getNMSPacket() {
        return this.packet;
    }

    public WrappedPacket getWrappedPacket() {
        return PacketDirection.WRITE.getWrappedFromNMS(this.player, this.getPacketId(), this.packet);
    }

    /**
     * Get the class of the NMS packet object.
     * Deprecated because it is useless, rather use getNMSPacket().getClass().
     *
     * @return packet object class
     */
    @Deprecated
    public Class<?> getNMSPacketClass() {
        return packet.getClass();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}

