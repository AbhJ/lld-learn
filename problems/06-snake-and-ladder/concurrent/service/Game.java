/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/Game.java — Turn-based game with AtomicInteger for strict turn order enforcement

package service;

import model.Board;
import model.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Game {
    private final Board board;                   // final = safe publication; board reference never changes
    private final List<Player> players;          // final = list reference stable; safe to read from any thread
    private final AtomicInteger currentPlayerIndex; // AtomicInteger = CAS-based turn tracking without locking
    private final List<String> moveLog;          // final = reference stable; mutations guarded by moveLock
    private final AtomicInteger validMoves;      // AtomicInteger = lock-free thread-safe counter
    private final AtomicInteger rejectedMoves;   // AtomicInteger = lock-free thread-safe counter
    private final ReentrantLock moveLock;         // ReentrantLock = ensures CAS + log are atomic together
    private volatile boolean gameOver;           // volatile = all threads see game-over immediately

    public Game(Board board, List<Player> players) {
        this.board = board;
        this.players = players;
        this.currentPlayerIndex = new AtomicInteger(0);
        this.moveLog = new ArrayList<>();
        this.validMoves = new AtomicInteger(0);
        this.rejectedMoves = new AtomicInteger(0);
        this.moveLock = new ReentrantLock();
        this.gameOver = false;
    }

    /**
     * Attempt to make a move. Only the player whose turn it is can move.
     * Uses CAS on currentPlayerIndex to claim the turn, with a lock to ensure
     * the move log stays in strict turn order.
     * Returns true if the move was accepted.
     */
    public boolean attemptMove(Player player) {
        if (gameOver) return false;

        int expectedIndex = player.getId();

        // Quick check without lock — fast rejection for wrong-turn players
        if (currentPlayerIndex.get() != expectedIndex) {
            rejectedMoves.incrementAndGet();
            return false;
        }

        // Acquire lock to ensure CAS + log addition are atomic
        moveLock.lock();
        try {
            if (gameOver) return false;

            // Re-check under lock
            if (currentPlayerIndex.get() != expectedIndex) {
                rejectedMoves.incrementAndGet();
                return false;
            }

            // CAS to claim this turn — advance to next player
            int nextIndex = (expectedIndex + 1) % players.size();
            if (!currentPlayerIndex.compareAndSet(expectedIndex, nextIndex)) {
                rejectedMoves.incrementAndGet();
                return false;
            }

            // Roll dice and move
            int roll = ThreadLocalRandom.current().nextInt(1, 7);
            int newPos = player.getPosition() + roll;

            if (newPos > board.getSize()) {
                moveLog.add(player.getName() + " rolled " + roll + " but stays at " + player.getPosition());
            } else {
                int finalPos = board.getFinalPosition(newPos);
                player.setPosition(finalPos);
                String extra = (finalPos != newPos) ? " (moved to " + finalPos + " via snake/ladder)" : "";
                moveLog.add(player.getName() + " rolled " + roll + " -> pos " + finalPos + extra);

                if (finalPos == board.getSize()) {
                    gameOver = true;
                    moveLog.add(player.getName() + " WINS!");
                }
            }

            validMoves.incrementAndGet();
            return true;
        } finally {
            moveLock.unlock();
        }
    }

    public boolean isGameOver() { return gameOver; }
    public int getCurrentPlayerIndex() { return currentPlayerIndex.get(); }
    public List<String> getMoveLog() { return new ArrayList<>(moveLog); }
    public int getValidMoves() { return validMoves.get(); }
    public int getRejectedMoves() { return rejectedMoves.get(); }
}
