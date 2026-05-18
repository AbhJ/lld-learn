/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/WinChecker.java — Full board scan to check winner (O(N) per check)

class WinChecker {

    public static Symbol checkWinner(Board board) { // static = utility; no instance state needed
        int size = board.getSize();

        // Check rows
        for (int r = 0; r < size; r++) {
            Symbol first = board.getSymbolAt(r, 0);
            if (first != null) {
                boolean win = true;
                for (int c = 1; c < size; c++) {
                    if (board.getSymbolAt(r, c) != first) { win = false; break; }
                }
                if (win) return first;
            }
        }

        // Check columns
        for (int c = 0; c < size; c++) {
            Symbol first = board.getSymbolAt(0, c);
            if (first != null) {
                boolean win = true;
                for (int r = 1; r < size; r++) {
                    if (board.getSymbolAt(r, c) != first) { win = false; break; }
                }
                if (win) return first;
            }
        }

        // Check main diagonal
        Symbol first = board.getSymbolAt(0, 0);
        if (first != null) {
            boolean win = true;
            for (int i = 1; i < size; i++) {
                if (board.getSymbolAt(i, i) != first) { win = false; break; }
            }
            if (win) return first;
        }

        // Check anti-diagonal
        first = board.getSymbolAt(0, size - 1);
        if (first != null) {
            boolean win = true;
            for (int i = 1; i < size; i++) {
                if (board.getSymbolAt(i, size - 1 - i) != first) { win = false; break; }
            }
            if (win) return first;
        }

        return null;
    }

    public static String getWinDescription(Board board) {
        int size = board.getSize();
        for (int r = 0; r < size; r++) {
            Symbol first = board.getSymbolAt(r, 0);
            if (first != null) {
                boolean win = true;
                for (int c = 1; c < size; c++) { if (board.getSymbolAt(r, c) != first) { win = false; break; } }
                if (win) return first + " wins (row " + r + ")";
            }
        }
        for (int c = 0; c < size; c++) {
            Symbol first = board.getSymbolAt(0, c);
            if (first != null) {
                boolean win = true;
                for (int r = 1; r < size; r++) { if (board.getSymbolAt(r, c) != first) { win = false; break; } }
                if (win) return first + " wins (column " + c + ")";
            }
        }
        Symbol d = board.getSymbolAt(0, 0);
        if (d != null) { boolean w = true; for (int i = 1; i < size; i++) { if (board.getSymbolAt(i, i) != d) { w = false; break; } } if (w) return d + " wins (main diagonal)"; }
        d = board.getSymbolAt(0, size - 1);
        if (d != null) { boolean w = true; for (int i = 1; i < size; i++) { if (board.getSymbolAt(i, size - 1 - i) != d) { w = false; break; } } if (w) return d + " wins (anti-diagonal)"; }
        return null;
    }
}
