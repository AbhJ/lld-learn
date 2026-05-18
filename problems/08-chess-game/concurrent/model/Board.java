/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Board.java — Simplified chess board for concurrent move/timeout demonstration

package model;

public class Board {
    private final String[][] grid;      // final = grid reference never changes; safe publication
    private int moveCount;              // guarded by synchronized block in GameController

    public Board() {
        this.grid = new String[8][8];
        this.moveCount = 0;
        initBoard();
    }

    private void initBoard() {
        // Simplified: just track piece presence
        for (int c = 0; c < 8; c++) {
            grid[1][c] = "WP"; // white pawns
            grid[6][c] = "BP"; // black pawns
        }
        grid[0][4] = "WK"; // white king
        grid[7][4] = "BK"; // black king
    }

    public boolean applyMove(Move move) {
        String piece = grid[move.getFromRow()][move.getFromCol()];
        if (piece == null) return false;
        grid[move.getToRow()][move.getToCol()] = piece;
        grid[move.getFromRow()][move.getFromCol()] = null;
        moveCount++;
        return true;
    }

    public int getMoveCount() { return moveCount; }

    public String getPiece(int row, int col) { return grid[row][col]; }
}
