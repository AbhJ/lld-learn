/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/Game.java — Thread-safe tic-tac-toe with CAS placement and ReentrantLock for turns

package service;

import model.Board;
import model.Board.Symbol;
import model.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Game {
    private final Board board;                   // final = board ref stable across threads
    private final Player playerX;                // final = player identity never changes
    private final Player playerO;                // final = player identity never changes
    private final ReentrantLock turnLock;         // ReentrantLock = mutual exclusion for turn logic
    private volatile Symbol currentTurn;         // volatile = turn change visible to all threads immediately
    private final List<String> moveLog;          // synchronizedList = thread-safe append/read
    private final AtomicInteger successfulMoves; // AtomicInteger = lock-free thread-safe counter
    private final AtomicInteger rejectedMoves;   // AtomicInteger = lock-free thread-safe counter
    private volatile boolean gameOver;           // volatile = game-over flag visible across threads
    private volatile String result;              // volatile = result string visible to all threads

    public Game(Board board, Player playerX, Player playerO) {
        this.board = board;
        this.playerX = playerX;
        this.playerO = playerO;
        this.turnLock = new ReentrantLock();
        this.currentTurn = Symbol.X; // X goes first
        this.moveLog = Collections.synchronizedList(new ArrayList<>());
        this.successfulMoves = new AtomicInteger(0);
        this.rejectedMoves = new AtomicInteger(0);
        this.gameOver = false;
        this.result = "IN_PROGRESS";
    }

    /**
     * Attempt to place a symbol. Uses ReentrantLock for turn enforcement
     * and CAS on the board cell to prevent overwrites.
     */
    public boolean attemptMove(Player player, int row, int col) {
        if (gameOver) {
            rejectedMoves.incrementAndGet();
            return false;
        }

        turnLock.lock();
        try {
            if (gameOver) {
                rejectedMoves.incrementAndGet();
                return false;
            }

            // Check turn
            if (player.getSymbol() != currentTurn) {
                rejectedMoves.incrementAndGet();
                return false;
            }

            // CAS to place — prevents overwrite even without turn lock
            boolean placed = board.tryPlace(row, col, player.getSymbol());
            if (!placed) {
                rejectedMoves.incrementAndGet();
                return false;
            }

            moveLog.add(player.getName() + " placed " + player.getSymbol() + " at (" + row + "," + col + ")");
            successfulMoves.incrementAndGet();

            // Check win
            Symbol winner = board.checkWinner();
            if (winner != Symbol.EMPTY) {
                gameOver = true;
                result = player.getName() + " WINS";
            } else if (board.isFull()) {
                gameOver = true;
                result = "DRAW";
            }

            // Switch turn
            currentTurn = (currentTurn == Symbol.X) ? Symbol.O : Symbol.X;
            return true;
        } finally {
            turnLock.unlock();
        }
    }

    public boolean isGameOver() { return gameOver; }
    public String getResult() { return result; }
    public Board getBoard() { return board; }
    public List<String> getMoveLog() { return moveLog; }
    public int getSuccessfulMoves() { return successfulMoves.get(); }
    public int getRejectedMoves() { return rejectedMoves.get(); }
    public Symbol getCurrentTurn() { return currentTurn; }
}
