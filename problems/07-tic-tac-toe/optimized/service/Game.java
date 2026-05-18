/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Game.java — Game controller using O(1) win check via board counters; observer fan-out

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

enum GameStatus {
    IN_PROGRESS, X_WINS, O_WINS, DRAW
}

/**
 * Optimized: After each move, uses Board.checkWinAt(row, col) for O(1) win check
 * instead of scanning the entire board.
 */
class Game {
    private Board board;
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private Stack<Move> moveHistory;    // Stack = LIFO; O(1) push/pop for undo support
    private GameStatus status;

    // Observer: fan-out for game events (move, win, draw).
    private final List<GameObserver> observers = new ArrayList<>();

    public Game(int boardSize, Player player1, Player player2) {
        this.board = new Board(boardSize);
        this.player1 = player1;
        this.player2 = player2;
        this.currentPlayer = player1;
        this.moveHistory = new Stack<>();
        this.status = GameStatus.IN_PROGRESS;
    }

    public boolean playTurn() {
        if (status != GameStatus.IN_PROGRESS) return false;
        int[] pos = currentPlayer.getMove(board);
        if (pos == null) return false;

        Move move = new Move(board, pos[0], pos[1], currentPlayer.getSymbol());
        if (!move.execute()) {
            System.out.println("  Invalid move: " + move);
            return false;
        }
        moveHistory.push(move);
        Player mover = currentPlayer;
        for (GameObserver o : observers) o.onMove(mover, pos[0], pos[1], mover.getSymbol());

        // O(1) win check using counters instead of full board scan
        Symbol winner = board.checkWinAt(pos[0], pos[1]);
        if (winner != null) {
            status = (winner == Symbol.X) ? GameStatus.X_WINS : GameStatus.O_WINS;
            for (GameObserver o : observers) o.onWin(mover, winner);
            return true;
        }
        if (board.isFull()) {
            status = GameStatus.DRAW;
            for (GameObserver o : observers) o.onDraw();
            return true;
        }
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
        return true;
    }

    public boolean undoLastMove() {
        if (moveHistory.isEmpty()) return false;
        Move lastMove = moveHistory.pop();
        lastMove.undo();
        status = GameStatus.IN_PROGRESS;
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
        return true;
    }

    public void playFullGame(boolean verbose) {
        while (status == GameStatus.IN_PROGRESS) {
            boolean moved = playTurn();
            if (!moved) break;
            if (verbose && !moveHistory.isEmpty()) {
                Move last = moveHistory.peek();
                Player who = (currentPlayer == player1) ? player2 : player1;
                if (status != GameStatus.IN_PROGRESS) who = (currentPlayer == player1) ? player2 : player1;
                System.out.println("  " + who.getName() + " plays " + last);
            }
        }
    }

    public Board getBoard() { return board; }
    public GameStatus getStatus() { return status; }
    public Player getCurrentPlayer() { return currentPlayer; }
    public Stack<Move> getMoveHistory() { return moveHistory; }

    public String getStatusMessage() {
        switch (status) {
            case X_WINS: return "X wins!";
            case O_WINS: return "O wins!";
            case DRAW: return "Draw!";
            default: return "Game in progress";
        }
    }

    // === Observer plumbing ===
    public void addObserver(GameObserver observer) { observers.add(observer); }
    public void removeObserver(GameObserver observer) { observers.remove(observer); }
}
