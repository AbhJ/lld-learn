/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Queue.java — Separate play queue for user-queued songs
import java.util.LinkedList;

public class Queue {
    private LinkedList<Song> queue;           // LinkedList = O(1) enqueue/dequeue for play queue

    public Queue() { this.queue = new LinkedList<>(); }
    public void enqueue(Song song) { queue.addLast(song); }
    public Song dequeue() { if (queue.isEmpty()) return null; return queue.removeFirst(); }
    public boolean isEmpty() { return queue.isEmpty(); }
    public int size() { return queue.size(); }
    public void clear() { queue.clear(); }
}
