/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Move.java — Immutable chess move

package model;

public class Move {
    private final int fromRow, fromCol, toRow, toCol; // final = immutable; safe to share across threads
    private final String player;        // final = immutable; no synchronization needed to read

    public Move(int fromRow, int fromCol, int toRow, int toCol, String player) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.player = player;
    }

    public int getFromRow() { return fromRow; }
    public int getFromCol() { return fromCol; }
    public int getToRow() { return toRow; }
    public int getToCol() { return toCol; }
    public String getPlayer() { return player; }

    @Override
    public String toString() {
        return player + ": (" + fromRow + "," + fromCol + ")->(" + toRow + "," + toCol + ")";
    }
}
