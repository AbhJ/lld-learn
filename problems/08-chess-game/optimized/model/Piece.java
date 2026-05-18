/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Piece.java — Abstract base for all chess pieces with movement validation

abstract class Piece {                  // abstract = can't create Piece directly; must be King, Queen, etc.
    protected Color color;              // protected = subclasses can access color directly
    protected Position position;        // protected = subclasses need position for move validation

    public Piece(Color color, Position position) { this.color = color; this.position = position; }

    public abstract boolean isValidMove(Position to, Board board);
    public abstract char getSymbol();
    public abstract String getName();

    public Color getColor() { return color; }
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }

    protected boolean isDiagonalMove(Position from, Position to) {
        return Math.abs(from.getRow() - to.getRow()) == Math.abs(from.getCol() - to.getCol());
    }

    protected boolean isStraightMove(Position from, Position to) {
        return from.getRow() == to.getRow() || from.getCol() == to.getCol();
    }

    protected boolean isPathClear(Position from, Position to, Board board) {
        int rowDir = Integer.signum(to.getRow() - from.getRow());
        int colDir = Integer.signum(to.getCol() - from.getCol());
        int r = from.getRow() + rowDir;
        int c = from.getCol() + colDir;
        while (r != to.getRow() || c != to.getCol()) {
            if (board.getPieceAt(new Position(r, c)) != null) return false;
            r += rowDir;
            c += colDir;
        }
        return true;
    }

    @Override
    public String toString() { return color + " " + getName() + " at " + position; }
}
