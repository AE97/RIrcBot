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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

/**
 * @author Lord_Ralex
 */
public class NetworkProcessor extends Thread {

    private final Queue<Node> queue = new ArrayDeque<>();
    private final Deque<Node> freeNodes = new java.util.LinkedList<>();

    public NetworkProcessor(int initial) {
        for (int i = 0; i < initial; i++) {
            freeNodes.add(new Node());
        }
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            if (queue.isEmpty()) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException ex) {
                }
            }
            Node next = queue.poll();
            if (next == null) {
                continue;
            }
            //process
            freeNodes.add(next);
        }
    }

    public void submit(String newMessage, long time) {
        boolean notify;
        synchronized (queue) {
            Node node = freeNodes.poll();
            if (node == null) {
                node = new Node();
            }
            node.message = newMessage;
            node.time = time;
            queue.add(node);
            notify = queue.peek() == node;
        }
        if (notify) {
            synchronized (this) {
                this.notify();
            }
        }
    }

    private class Node {

        String message;
        long time;
    }
}
