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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author Lord_Ralex
 */
public class NetworkProcessor extends Thread {

    private final Queue<Node> queue = new ArrayDeque<>();
    private final Deque<Node> freeNodes = new LinkedList<>();
    private final Deque<byte[]> freeBytes = new LinkedList<>();

    public NetworkProcessor(int initial) {
        super();
        for (int i = 0; i < initial; i++) {
            freeNodes.add(new Node());
            freeBytes.add(new byte[512]);
        }
        setDaemon(false);
    }

    @Override
    public void run() {
        List<Character> characters = new LinkedList<>();
        String message;
        int counter = 0;
        Character[] chars;
        char[] charArray;
        while (!isInterrupted()) {
            if (queue.isEmpty()) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException ex) {
                }
            }
            Node next;
            synchronized (queue) {
                next = queue.poll();
            }
            if (next == null) {
                continue;
            }
            for (; counter < next.message.length; counter++) {
                if (next.message[counter] >= 0) {
                    characters.add((char) next.message[counter]);
                } else {
                    break;
                }
            }
            chars = (Character[]) characters.toArray(new Character[characters.size()]);
            charArray = new char[chars.length];
            for (counter = 0; counter < chars.length; counter++) {
                charArray[counter] = chars[counter].charValue();
            }
            message = new String(charArray);
            //process message now
            synchronized (freeNodes) {
                freeNodes.add(next);
            }
            synchronized (freeBytes) {
                freeBytes.add(next.message);
            }
            counter = 0;
            message = null;
            characters.clear();
        }
    }

    public void submit(byte[] newMessage, long time) {
        boolean notify;
        byte[] messageCopy;
        synchronized (freeBytes) {
            messageCopy = freeBytes.poll();
        }

        if (messageCopy == null) {
            messageCopy = new byte[512];
        }
        int i;
        for (i = 0; i < newMessage.length; i++) {
            messageCopy[i] = newMessage[i];
        }
        for (; i < messageCopy.length; i++) {
            messageCopy[i] = -1;
        }
        Node node;
        synchronized (freeNodes) {
            node = freeNodes.poll();
        }
        if (node == null) {
            node = new Node();
        }
        node.message = messageCopy;
        node.time = time;
        synchronized (queue) {
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

        byte[] message;
        long time;
    }
}
