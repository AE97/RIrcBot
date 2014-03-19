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
import java.io.InputStream;
import java.net.Socket;
import java.util.logging.Level;
import net.ae97.rircbot.RIrcBot;
import net.ae97.rircbot.server.Server;

/**
 * @author Lord_Ralex
 */
public final class InputNetworkThread extends Thread {

    private final NetworkProcessor processor;
    private volatile Socket socket;
    private final Server server;

    public InputNetworkThread(Server ircServer) {
        super();
        server = ircServer;
        processor = new NetworkProcessor(20);
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
        byte[] bytes = new byte[512];
        try (InputStream in = socket.getInputStream()) {
            while (!interrupted()) {
                in.read(bytes);
                processor.submit(bytes, System.currentTimeMillis());
            }
        } catch (IOException ex) {
            RIrcBot.getLogger().log(Level.SEVERE, "Input has encountered an error", ex);
            server.disconnect();
        }
    }

    public void shutdown() {
        synchronized (this) {
            if (isAlive() && !isInterrupted()) {
                interrupt();
                try {
                    socket.shutdownInput();
                } catch (IOException ex) {
                }
            }
        }
    }
}
