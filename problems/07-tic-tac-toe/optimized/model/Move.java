/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Move.java — Command pattern: execute/undo a move on the board

class Move {
    private int row;
    private int col;
    private Symbol symbol;
    private Board board;

    public Move(Board board, int row, int col, Symbol symbol) {
        this.board = board;
        this.row = row;
        this.col = col;
        this.symbol = symbol;
    }

    public boolean execute() { return board.placeSymbol(row, col, symbol); }
    public boolean undo() { return board.removeSymbol(row, col); }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public Symbol getSymbol() { return symbol; }

    @Override
    public String toString() { return symbol + " at (" + row + "," + col + ")"; }
}
