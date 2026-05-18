/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Board.java — Board with snakes and ladders mapping

package model;

import java.util.HashMap;
import java.util.Map;

public class Board {
    private final int size;                      // final = safe publication; immutable after construction
    private final Map<Integer, Integer> snakes;  // final = reference won't change; safe to share across threads
    private final Map<Integer, Integer> ladders; // final = reference won't change; safe to share across threads

    public Board(int size) {
        this.size = size;
        this.snakes = new HashMap<>();
        this.ladders = new HashMap<>();
    }

    public void addSnake(int head, int tail) {
        snakes.put(head, tail);
    }

    public void addLadder(int bottom, int top) {
        ladders.put(bottom, top);
    }

    public int getFinalPosition(int position) {
        if (snakes.containsKey(position)) return snakes.get(position);
        if (ladders.containsKey(position)) return ladders.get(position);
        return position;
    }

    public int getSize() { return size; }
}
