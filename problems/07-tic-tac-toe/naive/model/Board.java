/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Board.java — NxN game grid managing cell state

class Board {
    private Symbol[][] grid;            // private = grid hidden; access only via placeSymbol/getSymbolAt
    private int size;                   // private = board dimension fixed at construction
    private int filledCount;            // private = internal counter to detect draw quickly

    public Board(int size) {
        this.size = size;
        this.grid = new Symbol[size][size];
        this.filledCount = 0;
    }

    public boolean placeSymbol(int row, int col, Symbol symbol) {
        if (!isValidPosition(row, col)) return false;
        if (grid[row][col] != null) return false;
        grid[row][col] = symbol;
        filledCount++;
        return true;
    }

    public boolean removeSymbol(int row, int col) {
        if (!isValidPosition(row, col)) return false;
        if (grid[row][col] == null) return false;
        grid[row][col] = null;
        filledCount--;
        return true;
    }

    public Symbol getSymbolAt(int row, int col) {
        if (!isValidPosition(row, col)) return null;
        return grid[row][col];
    }

    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }

    public boolean isEmpty(int row, int col) {
        return isValidPosition(row, col) && grid[row][col] == null;
    }

    public boolean isFull() { return filledCount == size * size; }
    public int getSize() { return size; }

    public String display() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (c > 0) sb.append("|");
                sb.append(grid[r][c] == null ? " " : grid[r][c]);
            }
            sb.append("\n");
            if (r < size - 1) {
                for (int c = 0; c < size * 2 - 1; c++) sb.append("-");
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
