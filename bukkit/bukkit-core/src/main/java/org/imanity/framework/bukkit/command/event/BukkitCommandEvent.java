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

package org.imanity.framework.bukkit.command.event;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.imanity.framework.command.CommandEvent;

public class BukkitCommandEvent extends CommandEvent {
    public BukkitCommandEvent(CommandSender user, String command) {
        super(user, command);
    }

    public CommandSender getSender() {
        return (CommandSender) this.getUser();
    }

    public Player getPlayer() {
        return (Player) this.getUser();
    }

    @Override
    public void sendUsage(String usage) {
        this.getSender().sendMessage(ChatColor.RED + "Usage: " + usage);
    }

    @Override
    public void sendError(Throwable throwable) {
        this.getSender().sendMessage(ChatColor.RED + "It appears there was some issues processing your command...");
    }

    @Override
    public void sendNoPermission() {
        this.getSender().sendMessage(ChatColor.RED + "No permission.");
    }

    @Override
    public void sendInternalError(String message) {
        this.getSender().sendMessage(ChatColor.RED + message);
    }
}
