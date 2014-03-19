/*
 * Copyright (C) 2014 Lord_Ralex
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.ae97.rircbot.network;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.logging.Level;
import net.ae97.rircbot.RIrcBot;
import net.ae97.rircbot.server.Server;

/**
 * @author Lord_Ralex
 */
public class OutputNetworkThread extends Thread {

    private final LinkedList<byte[]> queue = new LinkedList<>();
    private final ByteBuffer buffer = ByteBuffer.allocate(4);
    private volatile Socket socket;
    private final Server server;

    public OutputNetworkThread(Server ircServer) {
        super();
        server = ircServer;
        setDaemon(false);
    }

    public void start(Socket s) {
        socket = s;
        super.start();
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            return;
        }
        try (OutputStream out = socket.getOutputStream()) {
            while (!interrupted()) {
                if (queue.isEmpty()) {
                    synchronized (this) {
                        try {
                            this.wait();
                        } catch (InterruptedException ex) {
                        }
                    }
                } else {
                    byte[] bytes;
                    synchronized (queue) {
                        bytes = queue.poll();
                    }
                    out.write(bytes);
                }
            }
        } catch (IOException ex) {
            RIrcBot.getLogger().log(Level.SEVERE, "Output has encountered an error", ex);
            server.disconnect();
        }
    }

    public void send(int i) throws IOException {
        byte[] temp;
        synchronized (buffer) {
            buffer.putInt(i);
            temp = buffer.array();
            buffer.clear();
        }
        send(temp);
    }

    public void send(byte... bytes) {
        boolean notify;
        synchronized (queue) {
            queue.add(bytes);
            notify = queue.peek() == bytes;
        }
        if (notify) {
            synchronized (this) {
                notify();
            }
        }
    }

    public void send(String string) throws IOException {
        send(string.getBytes(Charset.forName("UTF-8")));
    }

    public void shutdown() {
        synchronized (this) {
            if (isAlive() && !isInterrupted()) {
                interrupt();
                try {
                    socket.shutdownOutput();
                } catch (IOException ex) {
                }
            }
        }
    }
}
