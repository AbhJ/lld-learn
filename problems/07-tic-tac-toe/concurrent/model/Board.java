/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Board.java — 3x3 board using AtomicReference[][] for CAS-based cell placement

package model;

import java.util.concurrent.atomic.AtomicReference;

public class Board {
    public enum Symbol { EMPTY, X, O }   // enum inside class = tightly coupled to Board usage

    private final AtomicReference<Symbol>[][] cells; // AtomicReference = CAS-based cell; prevents race on placement
    private final int size;              // final = immutable after construction; safe publication

    @SuppressWarnings("unchecked")
    public Board(int size) {
        this.size = size;
        this.cells = new AtomicReference[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                cells[r][c] = new AtomicReference<>(Symbol.EMPTY);
            }
        }
    }

    /**
     * CAS-based cell placement. Only succeeds if cell is EMPTY.
     * Prevents two players from placing on the same cell.
     */
    public boolean tryPlace(int row, int col, Symbol symbol) {
        if (row < 0 || row >= size || col < 0 || col >= size) return false;
        return cells[row][col].compareAndSet(Symbol.EMPTY, symbol); // CAS = atomic swap only if still EMPTY
    }

    public Symbol getCell(int row, int col) {
        return cells[row][col].get();
    }

    public int getSize() { return size; }

    public boolean isFull() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (cells[r][c].get() == Symbol.EMPTY) return false;
            }
        }
        return true;
    }

    public Symbol checkWinner() {
        // Check rows
        for (int r = 0; r < size; r++) {
            Symbol first = cells[r][0].get();
            if (first != Symbol.EMPTY && first == cells[r][1].get() && first == cells[r][2].get()) {
                return first;
            }
        }
        // Check columns
        for (int c = 0; c < size; c++) {
            Symbol first = cells[0][c].get();
            if (first != Symbol.EMPTY && first == cells[1][c].get() && first == cells[2][c].get()) {
                return first;
            }
        }
        // Check diagonals
        Symbol center = cells[1][1].get();
        if (center != Symbol.EMPTY) {
            if (center == cells[0][0].get() && center == cells[2][2].get()) return center;
            if (center == cells[0][2].get() && center == cells[2][0].get()) return center;
        }
        return Symbol.EMPTY;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Symbol s = cells[r][c].get();
                sb.append(s == Symbol.EMPTY ? "." : s.name());
                if (c < size - 1) sb.append("|");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
