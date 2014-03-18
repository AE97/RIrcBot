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
package net.ae97.rircbot.event.connection;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import net.ae97.rircbot.event.Event;
import net.ae97.rircbot.listener.Listener;

/**
 * @author Lord_Ralex
 */
public class ConnectionOpenEvent implements Event {

    private static final List<Listener> handlers = new LinkedList<>();
    private final InetSocketAddress destination, source;

    public ConnectionOpenEvent(InetSocketAddress d, InetSocketAddress s) {
        destination = d;
        source = s;
    }

    public InetSocketAddress getSourceAddress() {
        return source;
    }

    public InetSocketAddress getDestinationAddress() {
        return destination;
    }

    @Override
    public List<Listener> getHandlerList() {
        return handlers;
    }

    public static List<Listener> getHandlers() {
        return handlers;
    }
}
