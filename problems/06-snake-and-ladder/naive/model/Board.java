/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Board.java — Game board with cells tracking snake/ladder positions

import java.util.ArrayList;
import java.util.List;

class Board {
    private int size;                   // private = only Board methods can read/change size
    private Cell[] cells;               // private = board internals hidden from outside classes
    private List<Snake> snakes;         // List = ordered collection; can grow dynamically
    private List<Ladder> ladders;       // private = external code must use addLadder() method

    public Board(int size) {
        this.size = size;
        this.cells = new Cell[size + 1];
        this.snakes = new ArrayList<>();
        this.ladders = new ArrayList<>();
        for (int i = 0; i <= size; i++) {
            cells[i] = new Cell(i);
        }
    }

    public void addSnake(int head, int tail) {
        Snake snake = new Snake(head, tail);
        snakes.add(snake);
        cells[head].setSnake(snake);
    }

    public void addLadder(int start, int end) {
        Ladder ladder = new Ladder(start, end);
        ladders.add(ladder);
        cells[start].setLadder(ladder);
    }

    public int getFinalPosition(int position) {
        if (position < 0 || position > size) return position;
        return cells[position].getFinalPosition();
    }

    public boolean isWinningPosition(int position) { return position == size; }
    public int getSize() { return size; }
    public List<Snake> getSnakes() { return snakes; }
    public List<Ladder> getLadders() { return ladders; }
    public Cell getCell(int position) { return cells[position]; }
}
