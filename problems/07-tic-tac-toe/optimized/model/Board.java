/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Board.java — Board with row/col/diagonal counters for O(1) win detection

/**
 * Optimized: Maintains per-row, per-col, and diagonal counters.
 * When a symbol is placed, increment its counter for that row/col/diag.
 * Win check is O(1): just check if any counter equals board size.
 */
class Board {
    private Symbol[][] grid;            // 2D array = O(1) access by row,col coordinates
    private int size;
    private int filledCount;            // int counter = O(1) full-board check vs scanning all cells
    // Counters for O(1) win detection: +1 for X, -1 for O
    private int[] rowCounts;            // int[] = O(1) per-row running total; avoids scanning row
    private int[] colCounts;            // int[] = O(1) per-col running total; avoids scanning col
    private int diagCount;              // single int = tracks main diagonal sum
    private int antiDiagCount;          // single int = tracks anti-diagonal sum

    public Board(int size) {
        this.size = size;
        this.grid = new Symbol[size][size];
        this.filledCount = 0;
        this.rowCounts = new int[size];
        this.colCounts = new int[size];
        this.diagCount = 0;
        this.antiDiagCount = 0;
    }

    public boolean placeSymbol(int row, int col, Symbol symbol) {
        if (!isValidPosition(row, col)) return false;
        if (grid[row][col] != null) return false;
        grid[row][col] = symbol;
        filledCount++;

        int delta = (symbol == Symbol.X) ? 1 : -1;
        rowCounts[row] += delta;
        colCounts[col] += delta;
        if (row == col) diagCount += delta;
        if (row + col == size - 1) antiDiagCount += delta;

        return true;
    }

    public boolean removeSymbol(int row, int col) {
        if (!isValidPosition(row, col)) return false;
        if (grid[row][col] == null) return false;

        Symbol symbol = grid[row][col];
        int delta = (symbol == Symbol.X) ? -1 : 1; // Reverse
        rowCounts[row] += delta;
        colCounts[col] += delta;
        if (row == col) diagCount += delta;
        if (row + col == size - 1) antiDiagCount += delta;

        grid[row][col] = null;
        filledCount--;
        return true;
    }

    /**
     * O(1) win check after a move at (row, col).
     */
    public Symbol checkWinAt(int row, int col) {
        if (Math.abs(rowCounts[row]) == size) return rowCounts[row] > 0 ? Symbol.X : Symbol.O;
        if (Math.abs(colCounts[col]) == size) return colCounts[col] > 0 ? Symbol.X : Symbol.O;
        if (row == col && Math.abs(diagCount) == size) return diagCount > 0 ? Symbol.X : Symbol.O;
        if (row + col == size - 1 && Math.abs(antiDiagCount) == size) return antiDiagCount > 0 ? Symbol.X : Symbol.O;
        return null;
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
