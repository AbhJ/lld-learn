/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Cell.java — Single board cell with optional snake/ladder connection

class Cell {
    private int position;               // private = only this class can read position directly
    private Snake snake;                // private = encapsulates snake reference; use setter
    private Ladder ladder;              // private = encapsulates ladder reference; use setter

    public Cell(int position) {
        this.position = position;
        this.snake = null;
        this.ladder = null;
    }

    public int getPosition() { return position; }
    public void setSnake(Snake snake) { this.snake = snake; }
    public void setLadder(Ladder ladder) { this.ladder = ladder; }
    public boolean hasSnake() { return snake != null; }
    public boolean hasLadder() { return ladder != null; }
    public Snake getSnake() { return snake; }
    public Ladder getLadder() { return ladder; }

    public int getFinalPosition() {
        if (hasSnake()) return snake.getTail();
        if (hasLadder()) return ladder.getEnd();
        return position;
    }
}
