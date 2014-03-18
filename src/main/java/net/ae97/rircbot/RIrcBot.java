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
package net.ae97.rircbot;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.SocketFactory;
import net.ae97.rircbot.configuration.file.YamlConfiguration;
import net.ae97.rircbot.event.connection.ConnectionClosedEvent;
import net.ae97.rircbot.event.connection.ConnectionOpenEvent;
import net.ae97.rircbot.network.InputNetworkThread;
import net.ae97.rircbot.network.OutputNetworkThread;
import net.ae97.rircbot.plugin.PluginManager;
import net.ae97.rircbot.processor.Processor;

/**
 * @author Lord_Ralex
 */
public final class RIrcBot {

    private final static Logger logger = Logger.getLogger("Rircbot");

    public static RIrcBot getInstance() {
        return Main.getBot();
    }

    public static Logger getLogger() {
        return logger;
    }

    public static PluginManager getPluginManager() {
        return getInstance().pluginManager;
    }

    private final InputNetworkThread inputNetworkThread;
    private final OutputNetworkThread outputNetworkThread;
    private final Processor processor;
    private final YamlConfiguration configuration;
    private final PluginManager pluginManager;

    protected RIrcBot() {
        configuration = new YamlConfiguration();
        processor = new Processor(configuration.getInt("core.processors", 2));
        inputNetworkThread = new InputNetworkThread();
        outputNetworkThread = new OutputNetworkThread();
        pluginManager = new PluginManager();
    }

    public void connect() throws IOException {
        Socket socket = null;
        InetAddress bindAddress = null;
        if (configuration.contains("server.bind")) {
            bindAddress = InetAddress.getByName(configuration.getString("server.bind", null));
        }
        SocketFactory factory = SocketFactory.getDefault();
        String dest = configuration.getString("server.ip");
        int port = configuration.getInt("server.port", 6667);
        for (InetAddress address : InetAddress.getAllByName(dest)) {
            try {
                if (bindAddress != null) {
                    socket = factory.createSocket(address, port, bindAddress, 0);
                } else {
                    socket = factory.createSocket(address, port);
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not connect to " + address.getHostAddress() + ":" + port, e);
            }
        }
        if (socket == null) {
            throw new IOException("Failed to connect to server: " + dest + ":" + port);
        }
        ConnectionOpenEvent event = new ConnectionOpenEvent(new InetSocketAddress(socket.getInetAddress(), socket.getPort()), new InetSocketAddress(socket.getLocalAddress(), socket.getLocalPort()));
        pluginManager.callEvent(event);
        inputNetworkThread.start(socket);
        outputNetworkThread.start(socket);
    }

    public void disconnect() {
        inputNetworkThread.shutdown();
        outputNetworkThread.shutdown();
        Socket socket = inputNetworkThread.getSocket();
        ConnectionClosedEvent event = new ConnectionClosedEvent(new InetSocketAddress(socket.getInetAddress(), socket.getPort()), new InetSocketAddress(socket.getLocalAddress(), socket.getLocalPort()));
        pluginManager.callEvent(event);
        processor.shutdown();
        try {
            inputNetworkThread.join();
        } catch (InterruptedException e) {
        }
        try {
            outputNetworkThread.join();
        } catch (InterruptedException e) {
        }
        try {
            processor.join();
        } catch (InterruptedException e) {
        }
        try {
            pluginManager.join();
        } catch (InterruptedException e) {
        }
    }
}
