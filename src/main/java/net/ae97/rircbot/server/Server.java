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
package net.ae97.rircbot.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import net.ae97.rircbot.RIrcBot;
import net.ae97.rircbot.event.connection.ConnectionClosedEvent;
import net.ae97.rircbot.event.connection.ConnectionOpenEvent;
import net.ae97.rircbot.network.InputNetworkThread;
import net.ae97.rircbot.network.OutputNetworkThread;
import net.ae97.rircbot.recipient.Channel;
import net.ae97.rircbot.recipient.User;

/**
 * @author Lord_Ralex
 */
public final class Server {

    private InetSocketAddress destination;
    private InetSocketAddress source;
    private final Socket socket = new Socket();
    private final List<Channel> channels = new LinkedList<>();
    private final List<User> users = new LinkedList<>();
    private final InputNetworkThread inputNetworkThread;
    private final OutputNetworkThread outputNetworkThread;
    private final RIrcBot bot;

    public Server(RIrcBot instance) {
        bot = instance;
        inputNetworkThread = new InputNetworkThread(this);
        outputNetworkThread = new OutputNetworkThread(this);
    }

    public void connect(String destinationIp, int destinationPort, String bindIP) throws IOException {
        destination = null;
        if (bindIP != null) {
            source = new InetSocketAddress(InetAddress.getByName(bindIP), 0);
        } else {
            source = new InetSocketAddress(InetAddress.getLocalHost(), 0);
        }
        socket.bind(source);
        for (InetAddress address : InetAddress.getAllByName(destinationIp)) {
            try {
                destination = new InetSocketAddress(address, destinationPort);
                socket.connect(destination);
            } catch (IOException e) {
                RIrcBot.getLogger().log(Level.WARNING, "Could not connect to " + address.getHostAddress() + ":" + destinationPort, e);
                destination = null;
            }
        }
        if (destination == null) {
            throw new IOException("Failed to connect to server: " + destinationIp + ":" + destinationPort);
        }
        ConnectionOpenEvent event = new ConnectionOpenEvent(new InetSocketAddress(socket.getInetAddress(), socket.getPort()), new InetSocketAddress(socket.getLocalAddress(), socket.getLocalPort()));
        bot.getPluginManager().callEvent(event);
        inputNetworkThread.start(socket);
        outputNetworkThread.start(socket);
    }

    public void connect(String destinationIp, int destinationPort) throws IOException {
        connect(destinationIp, destinationPort, null);
    }

    public void disconnect() {
        inputNetworkThread.shutdown();
        outputNetworkThread.shutdown();
        ConnectionClosedEvent event = new ConnectionClosedEvent(new InetSocketAddress(socket.getInetAddress(), socket.getPort()), new InetSocketAddress(socket.getLocalAddress(), socket.getLocalPort()));
        bot.getPluginManager().callEvent(event);
    }

    public User getUser(String nick) {
        User user = null;
        for (User u : users) {
            if (u.getNick().equalsIgnoreCase(nick)) {
                return u;
            }
        }
        if (user == null) {
            user = User.getUser(this, nick);
            users.add(user);
        }
        return user;
    }

    public Channel getChannel(String name) {
        Channel channel = null;
        for (Channel c : channels) {
            if (c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        if (channel == null) {
            channel = Channel.getChannel(this, name);
            channels.add(channel);
        }
        return channel;
    }
}
