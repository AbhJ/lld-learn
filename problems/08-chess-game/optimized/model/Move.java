/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Move.java — Captures a chess move for history and undo

class Move {
    private Position from;
    private Position to;
    private Piece movedPiece;
    private Piece capturedPiece;
    private boolean wasFirstMove;

    public Move(Position from, Position to, Piece movedPiece, Piece capturedPiece) {
        this.from = from;
        this.to = to;
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.wasFirstMove = (movedPiece instanceof Pawn) && !((Pawn) movedPiece).hasMoved();
    }

    public Position getFrom() { return from; }
    public Position getTo() { return to; }
    public Piece getMovedPiece() { return movedPiece; }
    public Piece getCapturedPiece() { return capturedPiece; }
    public boolean wasFirstMove() { return wasFirstMove; }

    @Override
    public String toString() {
        String s = movedPiece.getName() + " " + from + "->" + to;
        if (capturedPiece != null) s += " x" + capturedPiece.getName();
        return s;
    }
}
