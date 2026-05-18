/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Bishop.java — Bishop: diagonal lines

class Bishop extends Piece {            // extends = inherits from Piece; moves diagonally
    public Bishop(Color color, Position position) { super(color, position); }

    @Override
    public boolean isValidMove(Position to, Board board) {
        if (!isDiagonalMove(position, to)) return false;
        return isPathClear(position, to, board);
    }

    @Override
    public char getSymbol() { return color == Color.WHITE ? 'B' : 'b'; }
    @Override
    public String getName() { return "Bishop"; }
}
