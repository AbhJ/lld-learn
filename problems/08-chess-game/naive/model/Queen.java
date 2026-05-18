/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Queen.java — Queen: combines rook and bishop movement

class Queen extends Piece {             // extends = inherits from Piece; combines Rook + Bishop
    public Queen(Color color, Position position) { super(color, position); }

    @Override
    public boolean isValidMove(Position to, Board board) {
        if (!isStraightMove(position, to) && !isDiagonalMove(position, to)) return false;
        return isPathClear(position, to, board);
    }

    @Override
    public char getSymbol() { return color == Color.WHITE ? 'Q' : 'q'; }
    @Override
    public String getName() { return "Queen"; }
}
