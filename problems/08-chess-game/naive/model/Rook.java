/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Rook.java — Rook: horizontal and vertical lines

class Rook extends Piece {              // extends = inherits from Piece; moves in straight lines
    public Rook(Color color, Position position) { super(color, position); }

    @Override
    public boolean isValidMove(Position to, Board board) {
        if (!isStraightMove(position, to)) return false;
        return isPathClear(position, to, board);
    }

    @Override
    public char getSymbol() { return color == Color.WHITE ? 'R' : 'r'; }
    @Override
    public String getName() { return "Rook"; }
}
