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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.ae97.rircbot.RIrcBot;
import net.ae97.rircbot.event.Event;
import net.ae97.rircbot.event.EventHandler;
import net.ae97.rircbot.event.EventProcessor;
import net.ae97.rircbot.listener.Listener;

/**
 * @author Lord_Ralex
 */
public class PluginManager {

    private final List<PluginHolder> pluginList = new LinkedList<>();
    private final EventProcessor eventProcessor;

    public PluginManager() {
        eventProcessor = new EventProcessor();
    }

    public void loadPlugin(Plugin pl) {
        unloadPlugin(pl);
        pluginList.add(new PluginHolder(pl));
    }

    public boolean unloadPlugin(Plugin pl) {
        unregisterListeners(pl);
        return pluginList.remove(getPluginHolder(pl));
    }

    public Plugin getPlugin(String name) {
        for (PluginHolder pl : pluginList) {
            if (pl.getPlugin().getName().equals(name)) {
                return pl.getPlugin();
            }
        }
        return null;
    }

    public void registerListener(Plugin plugin, Listener listener) {
        List<Listener> listeners = getListeners(plugin);
        listeners.add(listener);
        Method[] methods = listener.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(EventHandler.class)) {
                Class<?>[] params = method.getParameterTypes();
                if (params.length != 1) {
                    continue;
                }
                Class<?> cl = params[0];
                if (Event.class.isAssignableFrom(cl)) {
                    Class<? extends Event> event = (Class<? extends Event>) cl;
                    try {
                        Method eventHandlerMethod = event.getDeclaredMethod("getHandlers");
                        if (List.class.isAssignableFrom(eventHandlerMethod.getReturnType())) {
                            List listenerList = (List) eventHandlerMethod.invoke(null);
                            listenerList.add(listener);
                        }
                    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        Logger.getLogger(EventProcessor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    RIrcBot.getLogger().log(Level.SEVERE, "{0} tried to register non-event class {1}", new Object[]{listener.getClass().getName(), cl.getName()});
                }
            }
        }
    }

    public boolean unregisterListener(Plugin plugin, Listener listener) {
        List<Listener> listeners = getListeners(plugin);
        if (listeners != null) {
            Method[] methods = listener.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(EventHandler.class)) {
                    Class<?>[] params = method.getParameterTypes();
                    if (params.length != 1) {
                        continue;
                    }
                    Class<?> cl = params[0];
                    if (Event.class.isAssignableFrom(cl)) {
                        Class<? extends Event> event = (Class<? extends Event>) cl;
                        try {
                            Method eventHandlerMethod = event.getDeclaredMethod("getHandlers");
                            if (List.class.isAssignableFrom(eventHandlerMethod.getReturnType())) {
                                List listenerList = (List) eventHandlerMethod.invoke(null);
                                listenerList.remove(listener);
                            }
                        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            Logger.getLogger(EventProcessor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        RIrcBot.getLogger().log(Level.SEVERE, "{0} tried to register non-event class {1}", new Object[]{listener.getClass().getName(), cl.getName()});
                    }
                }
            }
        }
        return listeners != null && listeners.remove(listener);
    }

    public void unregisterListeners(Plugin plugin) {
        List<Listener> listeners = getListeners(plugin);
        if (listeners != null) {
            List<Listener> listenerCopy = new LinkedList<>();
            for (Listener listener : listeners) {
                listenerCopy.add(listener);
            }
            for (Listener listener : listenerCopy) {
                unregisterListener(plugin, listener);
            }
        }
        getPluginHolder(plugin).getListeners().clear();
    }

    public List<Listener> getListeners(Plugin plugin) {
        return getPluginHolder(plugin).getListeners();
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

    private PluginHolder getPluginHolder(String name) {
        for (PluginHolder pl : pluginList) {
            if (pl.getPlugin().getName().equals(name)) {
                return pl;
            }
        }
        return null;
    }

    private PluginHolder getPluginHolder(Plugin pl) {
        return getPluginHolder(pl.getName());
    }
}
