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
package net.ae97.rircbot.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.ae97.rircbot.RIrcBot;
import net.ae97.rircbot.listener.Listener;
import net.ae97.rircbot.plugin.Plugin;

/**
 * @author Lord_Ralex
 */
public class EventProcessor extends Thread {

    private final Map<Plugin, List<Listener>> enabledListeners = new ConcurrentHashMap<>();
    private final Queue<Event> eventQueue = new LinkedList<>();

    public EventProcessor() {
    }

    public void registerListener(Plugin plugin, Listener listener) {
        List<Listener> listeners = enabledListeners.get(plugin);
        if (listeners == null) {
            listeners = new LinkedList<>();
            enabledListeners.put(plugin, listeners);
        }
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
                    RIrcBot.getLogger().severe(listener.getClass().getName() + " tried to register non-event class " + cl.getName());
                }
            }
        }
    }

    public boolean unregisterListener(Plugin plugin, Listener listener) {
        List<Listener> listeners = enabledListeners.get(plugin);
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
                        RIrcBot.getLogger().severe(listener.getClass().getName() + " tried to register non-event class " + cl.getName());
                    }
                }
            }
        }
        return listeners != null && listeners.remove(listener);
    }

    public boolean unregisterListeners(Plugin plugin) {
        List<Listener> listeners = enabledListeners.get(plugin);
        if (listeners != null) {
            List<Listener> listenerCopy = new LinkedList<>();
            for (Listener listener : listeners) {
                listenerCopy.add(listener);
            }
            for (Listener listener : listenerCopy) {
                unregisterListener(plugin, listener);
            }
        }
        return enabledListeners.remove(plugin) != null;
    }

    public List<Listener> getListeners(Plugin plugin) {
        List<Listener> listeners = enabledListeners.get(plugin);
        if (listeners == null) {
            listeners = new LinkedList<>();
            enabledListeners.put(plugin, listeners);
        }
        return listeners;

    }

    @Override
    public void run() {
        while (!interrupted()) {
            Event event;
            synchronized (eventQueue) {
                event = eventQueue.poll();
            }
            if (event == null) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                    }
                }
                continue;
            }
            List<Listener> listeners = event.getHandlerList();
            for (Listener listener : listeners) {
                for (Method method : listener.getClass().getMethods()) {
                    if (method.isAnnotationPresent(EventHandler.class)) {
                        try {
                            EventHandler handler = method.getAnnotation(EventHandler.class);
                            if (handler.ignoreCancel() || (event instanceof CancellableEvent && !((CancellableEvent) event).isCancelled())) {
                                method.invoke(listener, event);
                            }
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            RIrcBot.getLogger().log(Level.SEVERE, "Error occurred while processing " + event.getClass().getSimpleName() + " in " + findPlugin(listener).getName(), ex);
                        }
                    }
                }
            }
        }
    }

    protected Plugin findPlugin(Listener listener) {
        for (Plugin pl : enabledListeners.keySet()) {
            if (enabledListeners.get(pl).contains(listener)) {
                return pl;
            }
        }
        return null;
    }

    public void fireEvent(Event event) {
        boolean notify;
        synchronized (eventQueue) {
            eventQueue.add(event);
            notify = eventQueue.peek() == event;
        }
        if (notify) {
            synchronized (this) {
                this.notify();
            }
        }
    }

    protected void shutdown() {
        synchronized (this) {
            this.interrupt();
        }
    }
}
