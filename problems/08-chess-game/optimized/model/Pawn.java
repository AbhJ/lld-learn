/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Pawn.java — Pawn: forward advance, diagonal capture, double first move

class Pawn extends Piece {
    private boolean hasMoved;

    public Pawn(Color color, Position position) { super(color, position); this.hasMoved = false; }

    @Override
    public boolean isValidMove(Position to, Board board) {
        int direction = (color == Color.WHITE) ? -1 : 1;
        int rowDiff = to.getRow() - position.getRow();
        int colDiff = Math.abs(to.getCol() - position.getCol());

        if (colDiff == 0) {
            if (rowDiff == direction) return board.getPieceAt(to) == null;
            if (!hasMoved && rowDiff == 2 * direction) {
                Position intermediate = new Position(position.getRow() + direction, position.getCol());
                return board.getPieceAt(intermediate) == null && board.getPieceAt(to) == null;
            }
        }
        if (colDiff == 1 && rowDiff == direction) {
            Piece target = board.getPieceAt(to);
            return target != null && target.getColor() != this.color;
        }
        return false;
    }

    public void markMoved() { this.hasMoved = true; }
    public boolean hasMoved() { return hasMoved; }

    @Override
    public char getSymbol() { return color == Color.WHITE ? 'P' : 'p'; }
    @Override
    public String getName() { return "Pawn"; }
}
