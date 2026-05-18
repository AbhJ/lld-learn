/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/MoveValidator.java — Validates moves by iterating all pieces (naive linear scan)

import java.util.List;

class MoveValidator {

    public static boolean isValidMove(Board board, Position from, Position to, Color currentPlayer) { // static = stateless utility; no instance needed
        Piece piece = board.getPieceAt(from);
        if (piece == null) return false;
        if (piece.getColor() != currentPlayer) return false;
        Piece target = board.getPieceAt(to);
        if (target != null && target.getColor() == currentPlayer) return false;
        if (!piece.isValidMove(to, board)) return false;
        if (wouldBeInCheck(board, from, to, currentPlayer)) return false;
        return true;
    }

    public static boolean isInCheck(Board board, Color color) {
        Position kingPos = board.findKing(color);
        if (kingPos == null) return false;
        return isSquareAttacked(board, kingPos, color.opposite());
    }

    public static boolean isSquareAttacked(Board board, Position pos, Color attackingColor) {
        // Naive: iterate ALL pieces of attacking color
        List<Piece> attackers = board.getPieces(attackingColor);
        for (Piece attacker : attackers) {
            if (attacker.isValidMove(pos, board)) return true;
        }
        return false;
    }

    private static boolean wouldBeInCheck(Board board, Position from, Position to, Color color) {
        Piece moving = board.getPieceAt(from);
        Piece captured = board.getPieceAt(to);
        board.setPieceAt(to, moving);
        board.removePiece(from);
        Position originalPos = moving.getPosition();
        moving.setPosition(to);

        boolean inCheck = isInCheck(board, color);

        moving.setPosition(originalPos);
        board.setPieceAt(from, moving);
        board.setPieceAt(to, captured);
        return inCheck;
    }

    public static boolean isCheckmate(Board board, Color color) {
        if (!isInCheck(board, color)) return false;
        List<Piece> pieces = board.getPieces(color);
        for (Piece piece : pieces) {
            Position from = piece.getPosition();
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    Position to = new Position(r, c);
                    if (from.equals(to)) continue;
                    Piece target = board.getPieceAt(to);
                    if (target != null && target.getColor() == color) continue;
                    if (piece.isValidMove(to, board)) {
                        if (!wouldBeInCheck(board, from, to, color)) return false;
                    }
                }
            }
        }
        return true;
    }
}
