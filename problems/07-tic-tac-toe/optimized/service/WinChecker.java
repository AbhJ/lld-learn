/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/WinChecker.java — O(1) win detection using board counters

/**
 * Optimized: Uses Board's internal counters for O(1) win check.
 * The static checkWinner method still provides backward compatibility
 * by scanning, but Game uses checkWinAt() for O(1) after each move.
 */
class WinChecker {

    /**
     * Legacy full-scan method for compatibility with tests that call it directly.
     * In practice, Game uses Board.checkWinAt() for O(1).
     */
    public static Symbol checkWinner(Board board) {
        int size = board.getSize();
        // Use counter-based check for each possible line
        for (int r = 0; r < size; r++) {
            Symbol first = board.getSymbolAt(r, 0);
            if (first != null) {
                boolean win = true;
                for (int c = 1; c < size; c++) { if (board.getSymbolAt(r, c) != first) { win = false; break; } }
                if (win) return first;
            }
        }
        for (int c = 0; c < size; c++) {
            Symbol first = board.getSymbolAt(0, c);
            if (first != null) {
                boolean win = true;
                for (int r = 1; r < size; r++) { if (board.getSymbolAt(r, c) != first) { win = false; break; } }
                if (win) return first;
            }
        }
        Symbol d = board.getSymbolAt(0, 0);
        if (d != null) { boolean w = true; for (int i = 1; i < size; i++) { if (board.getSymbolAt(i, i) != d) { w = false; break; } } if (w) return d; }
        d = board.getSymbolAt(0, size - 1);
        if (d != null) { boolean w = true; for (int i = 1; i < size; i++) { if (board.getSymbolAt(i, size - 1 - i) != d) { w = false; break; } } if (w) return d; }
        return null;
    }

    public static String getWinDescription(Board board) {
        int size = board.getSize();
        for (int r = 0; r < size; r++) {
            Symbol first = board.getSymbolAt(r, 0);
            if (first != null) { boolean w = true; for (int c = 1; c < size; c++) { if (board.getSymbolAt(r, c) != first) { w = false; break; } } if (w) return first + " wins (row " + r + ")"; }
        }
        for (int c = 0; c < size; c++) {
            Symbol first = board.getSymbolAt(0, c);
            if (first != null) { boolean w = true; for (int r = 1; r < size; r++) { if (board.getSymbolAt(r, c) != first) { w = false; break; } } if (w) return first + " wins (column " + c + ")"; }
        }
        Symbol d = board.getSymbolAt(0, 0);
        if (d != null) { boolean w = true; for (int i = 1; i < size; i++) { if (board.getSymbolAt(i, i) != d) { w = false; break; } } if (w) return d + " wins (main diagonal)"; }
        d = board.getSymbolAt(0, size - 1);
        if (d != null) { boolean w = true; for (int i = 1; i < size; i++) { if (board.getSymbolAt(i, size - 1 - i) != d) { w = false; break; } } if (w) return d + " wins (anti-diagonal)"; }
        return null;
    }
}
