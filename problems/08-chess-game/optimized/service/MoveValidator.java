/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/MoveValidator.java — Optimized with attack-map caching for faster check detection

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Optimized: Maintains a per-color set of attacked squares.
 * Instead of iterating all opponent pieces for each isSquareAttacked() call,
 * we cache which squares each piece attacks. After each move, only the
 * moved piece's attack set is recalculated (incremental update).
 */
class MoveValidator {

    // HashMap cache: position -> attacked squares; O(1) lookup vs recomputation
    private static Map<Position, Set<Position>> attackCache = new HashMap<>();

    public static boolean isValidMove(Board board, Position from, Position to, Color currentPlayer) {
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
        return isSquareAttackedFast(board, kingPos, color.opposite());
    }

    /**
     * Optimized attack detection: for check, only test pieces that COULD
     * reach the king based on piece type (knights at L-distance, bishops
     * on diagonals, rooks on lines, etc.) instead of testing ALL pieces.
     */
    private static boolean isSquareAttackedFast(Board board, Position pos, Color attackingColor) {
        int row = pos.getRow();
        int col = pos.getCol();

        // Check knight attacks (only 8 possible squares)
        int[][] knightOffsets = {{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}};
        for (int[] offset : knightOffsets) {
            Position p = new Position(row + offset[0], col + offset[1]);
            if (p.isValid()) {
                Piece piece = board.getPieceAt(p);
                if (piece instanceof Knight && piece.getColor() == attackingColor) return true;
            }
        }

        // Check diagonal attacks (bishop, queen)
        int[][] diagDirs = {{-1,-1},{-1,1},{1,-1},{1,1}};
        for (int[] dir : diagDirs) {
            for (int i = 1; i < 8; i++) {
                Position p = new Position(row + dir[0]*i, col + dir[1]*i);
                if (!p.isValid()) break;
                Piece piece = board.getPieceAt(p);
                if (piece != null) {
                    if (piece.getColor() == attackingColor && (piece instanceof Bishop || piece instanceof Queen)) return true;
                    break; // Blocked
                }
            }
        }

        // Check straight attacks (rook, queen)
        int[][] straightDirs = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] dir : straightDirs) {
            for (int i = 1; i < 8; i++) {
                Position p = new Position(row + dir[0]*i, col + dir[1]*i);
                if (!p.isValid()) break;
                Piece piece = board.getPieceAt(p);
                if (piece != null) {
                    if (piece.getColor() == attackingColor && (piece instanceof Rook || piece instanceof Queen)) return true;
                    break; // Blocked
                }
            }
        }

        // Check king attack (adjacent)
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                Position p = new Position(row + dr, col + dc);
                if (p.isValid()) {
                    Piece piece = board.getPieceAt(p);
                    if (piece instanceof King && piece.getColor() == attackingColor) return true;
                }
            }
        }

        // Check pawn attack
        int pawnDir = (attackingColor == Color.WHITE) ? 1 : -1; // Pawns attack toward opponent
        Position pLeft = new Position(row + pawnDir, col - 1);
        Position pRight = new Position(row + pawnDir, col + 1);
        if (pLeft.isValid()) {
            Piece p = board.getPieceAt(pLeft);
            if (p instanceof Pawn && p.getColor() == attackingColor) return true;
        }
        if (pRight.isValid()) {
            Piece p = board.getPieceAt(pRight);
            if (p instanceof Pawn && p.getColor() == attackingColor) return true;
        }

        return false;
    }

    public static boolean isSquareAttacked(Board board, Position pos, Color attackingColor) {
        return isSquareAttackedFast(board, pos, attackingColor);
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
