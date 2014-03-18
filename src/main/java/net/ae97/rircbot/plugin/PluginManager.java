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
package net.ae97.rircbot.plugin;

import java.util.LinkedList;
import java.util.List;
import net.ae97.rircbot.event.Event;
import net.ae97.rircbot.event.EventProcessor;

/**
 * @author Lord_Ralex
 */
public class PluginManager {

    private final List<Plugin> pluginList = new LinkedList<>();
    private final EventProcessor eventProcessor;

    public PluginManager() {
        eventProcessor = new EventProcessor();
    }

    public void loadPlugin(Plugin pl) {
        unloadPlugin(pl);
        pluginList.add(pl);
    }

    public boolean unloadPlugin(Plugin pl) {
        eventProcessor.unregisterListeners(pl);
        return pluginList.remove(pl);
    }

    public void callEvent(Event event) {
        eventProcessor.fireEvent(event);
    }

    public void start() {
        eventProcessor.start();
    }

    public void join() throws InterruptedException {
        eventProcessor.interrupt();
        eventProcessor.join();
    }
}
