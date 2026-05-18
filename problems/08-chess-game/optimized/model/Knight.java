/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Knight.java — Knight: L-shaped jumps

class Knight extends Piece {
    public Knight(Color color, Position position) { super(color, position); }

    @Override
    public boolean isValidMove(Position to, Board board) {
        int rowDiff = Math.abs(position.getRow() - to.getRow());
        int colDiff = Math.abs(position.getCol() - to.getCol());
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }

    @Override
    public char getSymbol() { return color == Color.WHITE ? 'N' : 'n'; }
    @Override
    public String getName() { return "Knight"; }
}
