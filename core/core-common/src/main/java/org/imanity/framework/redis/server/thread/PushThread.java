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

package org.imanity.framework.redis.server.thread;

import org.imanity.framework.ImanityCommon;
import org.imanity.framework.redis.server.ImanityServer;
import org.imanity.framework.redis.server.ServerHandler;
import org.redisson.api.RMap;

public class PushThread extends Thread {

    private final ServerHandler serverHandler;

    public PushThread(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;

        this.setDaemon(true);
        this.setName("Imanity Server Push Thread");
    }

    @Override
    public void run() {

        while (!ImanityCommon.PLATFORM.isShuttingDown()) {

            try {
                this.push();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            try {
                Thread.sleep(5000L);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

        }

    }

    public void shutdown() {
        ImanityServer server = serverHandler.getCurrentServer();
        this.serverHandler.getRedis()
                .getMap(ServerHandler.METADATA + ":" + server.getName())
                .clear();
    }

    private void push() {

        ImanityServer server = serverHandler.getCurrentServer();

        RMap<String, Object> map = this.serverHandler.getRedis().getMap(ServerHandler.METADATA + ":" + server.getName());
        map.put("onlinePlayers", server.getOnlinePlayers());
        map.put("maxPlayers", server.getMaxPlayers());
        map.put("state", server.getServerState().name());
        map.put("metadata", server.getMetadata());

    }
}
