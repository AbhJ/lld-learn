/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Board.java — Optimized board using HashMap<Integer,Integer> for O(1) jump lookups

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Optimized: Instead of Cell[] with object per position, uses a direct
 * HashMap<position, destination> for snake/ladder jumps. O(1) lookup
 * without creating Cell objects for every position.
 */
class Board {
    private int size;
    private Map<Integer, Integer> jumps; // HashMap = O(1) lookup; avoids Cell object per position
    private List<Snake> snakes;          // ArrayList = O(1) random access, good for iteration
    private List<Ladder> ladders;        // ArrayList = stores ladders for display/iteration

    public Board(int size) {
        this.size = size;
        this.jumps = new HashMap<>();     // HashMap chosen for O(1) position-to-destination mapping
        this.snakes = new ArrayList<>();  // ArrayList for simple append-and-iterate usage
        this.ladders = new ArrayList<>(); // ArrayList for simple append-and-iterate usage
    }

    public void addSnake(int head, int tail) {
        Snake snake = new Snake(head, tail);
        snakes.add(snake);
        jumps.put(head, tail); // O(1) lookup at runtime
    }

    public void addLadder(int start, int end) {
        Ladder ladder = new Ladder(start, end);
        ladders.add(ladder);
        jumps.put(start, end); // O(1) lookup at runtime
    }

    /**
     * O(1): Single HashMap lookup instead of Cell object delegation.
     */
    public int getFinalPosition(int position) {
        if (position < 0 || position > size) return position;
        return jumps.getOrDefault(position, position);
    }

    public boolean isWinningPosition(int position) { return position == size; }
    public int getSize() { return size; }
    public List<Snake> getSnakes() { return snakes; }
    public List<Ladder> getLadders() { return ladders; }
}
