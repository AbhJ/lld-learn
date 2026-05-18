/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Game.java — Chess game controller with turn management and check detection

import java.util.Stack;

class Game {
    private Board board;
    private Player whitePlayer;
    private Player blackPlayer;
    private Color currentTurn;
    private Stack<Move> moveHistory;    // Stack = O(1) push/pop for move undo support
    private boolean gameOver;
    private String result;

    public Game(Player white, Player black) {
        this.board = new Board();
        this.board.setupStandard();
        this.whitePlayer = white;
        this.blackPlayer = black;
        this.currentTurn = Color.WHITE;
        this.moveHistory = new Stack<>();
        this.gameOver = false;
    }

    public Game(Board board, Player white, Player black) {
        this.board = board;
        this.whitePlayer = white;
        this.blackPlayer = black;
        this.currentTurn = Color.WHITE;
        this.moveHistory = new Stack<>();
        this.gameOver = false;
    }

    public boolean makeMove(Position from, Position to) {
        if (gameOver) return false;
        if (!MoveValidator.isValidMove(board, from, to, currentTurn)) return false;

        Piece piece = board.getPieceAt(from);
        Piece captured = board.getPieceAt(to);
        Move move = new Move(from, to, piece, captured);
        moveHistory.push(move);

        board.setPieceAt(to, piece);
        board.removePiece(from);
        piece.setPosition(to);
        if (piece instanceof Pawn) ((Pawn) piece).markMoved();

        Color opponent = currentTurn.opposite();
        if (MoveValidator.isCheckmate(board, opponent)) {
            gameOver = true;
            result = currentTurn + " wins by checkmate!";
        }
        currentTurn = opponent;
        return true;
    }

    public boolean undoLastMove() {
        if (moveHistory.isEmpty()) return false;
        Move move = moveHistory.pop();
        Piece piece = move.getMovedPiece();
        board.setPieceAt(move.getFrom(), piece);
        piece.setPosition(move.getFrom());
        if (move.getCapturedPiece() != null) board.setPieceAt(move.getTo(), move.getCapturedPiece());
        else board.removePiece(move.getTo());
        currentTurn = currentTurn.opposite();
        gameOver = false;
        result = null;
        return true;
    }

    public Board getBoard() { return board; }
    public Color getCurrentTurn() { return currentTurn; }
    public boolean isGameOver() { return gameOver; }
    public String getResult() { return result; }
    public Stack<Move> getMoveHistory() { return moveHistory; }
    public boolean isInCheck() { return MoveValidator.isInCheck(board, currentTurn); }

    public String getCurrentStatus() {
        if (gameOver) return result;
        String status = currentTurn + "'s turn";
        if (isInCheck()) status += " (IN CHECK)";
        return status;
    }
}
