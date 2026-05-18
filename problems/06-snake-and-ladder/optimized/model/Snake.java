/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Snake.java — Snake connecting a higher cell to a lower cell

class Snake {
    private int head;
    private int tail;

    public Snake(int head, int tail) {
        if (head <= tail) throw new IllegalArgumentException("Snake head must be above tail: " + head + " -> " + tail);
        this.head = head;
        this.tail = tail;
    }

    public int getHead() { return head; }
    public int getTail() { return tail; }

    @Override
    public String toString() { return "Snake(" + head + " -> " + tail + ")"; }
}
