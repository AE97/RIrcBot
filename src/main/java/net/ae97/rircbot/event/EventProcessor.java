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
import java.util.logging.Level;
import net.ae97.rircbot.RIrcBot;
import net.ae97.rircbot.listener.Listener;

/**
 * @author Lord_Ralex
 */
public class EventProcessor extends Thread {

    private final LinkedList<Event> eventQueue = new LinkedList<>();

    public EventProcessor() {
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
                            RIrcBot.getLogger().log(Level.SEVERE, "Error occurred while processing " + event.getClass().getSimpleName(), (ex instanceof InvocationTargetException) ? ex.getCause() : ex);
                        }
                    }
                }
            }
        }
    }

    public void fireEvent(Event event) {
        boolean notify;
        synchronized (eventQueue) {
            if (PriorityEvent.class.isAssignableFrom(event.getClass())) {
                eventQueue.add(0, event);
                notify = true;
            } else {
                eventQueue.add(event);
                notify = eventQueue.peek() == event;
            }
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
