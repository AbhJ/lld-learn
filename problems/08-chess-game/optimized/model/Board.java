/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Board.java — 8x8 chess board managing piece placement and queries

import java.util.ArrayList;
import java.util.List;

class Board {
    private Piece[][] grid;             // 2D array = O(1) piece lookup by row,col

    public Board() { grid = new Piece[8][8]; }

    public void setupStandard() {
        grid[0][0] = new Rook(Color.BLACK, new Position(0, 0));
        grid[0][1] = new Knight(Color.BLACK, new Position(0, 1));
        grid[0][2] = new Bishop(Color.BLACK, new Position(0, 2));
        grid[0][3] = new Queen(Color.BLACK, new Position(0, 3));
        grid[0][4] = new King(Color.BLACK, new Position(0, 4));
        grid[0][5] = new Bishop(Color.BLACK, new Position(0, 5));
        grid[0][6] = new Knight(Color.BLACK, new Position(0, 6));
        grid[0][7] = new Rook(Color.BLACK, new Position(0, 7));
        for (int c = 0; c < 8; c++) grid[1][c] = new Pawn(Color.BLACK, new Position(1, c));
        for (int c = 0; c < 8; c++) grid[6][c] = new Pawn(Color.WHITE, new Position(6, c));
        grid[7][0] = new Rook(Color.WHITE, new Position(7, 0));
        grid[7][1] = new Knight(Color.WHITE, new Position(7, 1));
        grid[7][2] = new Bishop(Color.WHITE, new Position(7, 2));
        grid[7][3] = new Queen(Color.WHITE, new Position(7, 3));
        grid[7][4] = new King(Color.WHITE, new Position(7, 4));
        grid[7][5] = new Bishop(Color.WHITE, new Position(7, 5));
        grid[7][6] = new Knight(Color.WHITE, new Position(7, 6));
        grid[7][7] = new Rook(Color.WHITE, new Position(7, 7));
    }

    public Piece getPieceAt(Position pos) {
        if (!pos.isValid()) return null;
        return grid[pos.getRow()][pos.getCol()];
    }

    public void setPieceAt(Position pos, Piece piece) { grid[pos.getRow()][pos.getCol()] = piece; }
    public void removePiece(Position pos) { grid[pos.getRow()][pos.getCol()] = null; }

    public Position findKing(Color color) {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p instanceof King && p.getColor() == color) return new Position(r, c);
            }
        return null;
    }

    public List<Piece> getPieces(Color color) {
        List<Piece> pieces = new ArrayList<>();
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                if (grid[r][c] != null && grid[r][c].getColor() == color) pieces.add(grid[r][c]);
        return pieces;
    }

    public String display() {
        StringBuilder sb = new StringBuilder();
        sb.append("  a b c d e f g h\n");
        for (int r = 0; r < 8; r++) {
            sb.append((8 - r) + " ");
            for (int c = 0; c < 8; c++) {
                sb.append(grid[r][c] == null ? "." : "" + grid[r][c].getSymbol());
                if (c < 7) sb.append(" ");
            }
            sb.append(" " + (8 - r) + "\n");
        }
        sb.append("  a b c d e f g h");
        return sb.toString();
    }
}
