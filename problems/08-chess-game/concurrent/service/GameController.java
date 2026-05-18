/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/GameController.java — CAS-based game state transitions for move/timeout race

package service;

import model.Board;
import model.Move;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class GameController {
    public enum GameState {              // enum = fixed set of all possible game outcomes
        PLAYING, WHITE_WINS, BLACK_WINS, TIMEOUT_WHITE, TIMEOUT_BLACK, DRAW
    }

    private final Board board;                   // final = board ref stable; mutations guarded by synchronized
    private final AtomicReference<GameState> state; // AtomicReference = CAS ensures exactly one outcome wins
    private final List<String> eventLog;         // synchronizedList = thread-safe append from any thread
    private volatile String currentPlayer;       // volatile = turn change visible to all threads immediately

    public GameController() {
        this.board = new Board();
        this.state = new AtomicReference<>(GameState.PLAYING);
        this.eventLog = Collections.synchronizedList(new ArrayList<>());
        this.currentPlayer = "WHITE";
    }

    /**
     * Attempt to make a move. Only succeeds if game state is still PLAYING.
     * Uses CAS to ensure move and timeout don't both succeed.
     */
    public boolean makeMove(Move move) {
        if (state.get() != GameState.PLAYING) {
            eventLog.add("MOVE REJECTED (game over): " + move);
            return false;
        }

        // Simulate move processing
        synchronized (board) {
            if (state.get() != GameState.PLAYING) {
                eventLog.add("MOVE REJECTED (game ended during processing): " + move);
                return false;
            }

            boolean applied = board.applyMove(move);
            if (!applied) {
                eventLog.add("MOVE INVALID: " + move);
                return false;
            }

            eventLog.add("MOVE APPLIED: " + move);

            // Check for "win" condition (simplified: after 5 moves, declare winner)
            if (board.getMoveCount() >= 5) {
                GameState winState = move.getPlayer().equals("WHITE") ?
                        GameState.WHITE_WINS : GameState.BLACK_WINS;
                // CAS — only succeeds if still PLAYING (timeout might have fired)
                if (state.compareAndSet(GameState.PLAYING, winState)) {
                    eventLog.add("GAME OVER: " + winState + " (by move)");
                }
            }

            // Switch turn
            currentPlayer = currentPlayer.equals("WHITE") ? "BLACK" : "WHITE";
            return true;
        }
    }

    /**
     * Timer declares timeout for a player. Uses CAS so only one outcome wins.
     * If a move is being processed simultaneously, only one event determines the result.
     */
    public boolean declareTimeout(String timedOutPlayer) {
        GameState timeoutState = timedOutPlayer.equals("WHITE") ?
                GameState.TIMEOUT_WHITE : GameState.TIMEOUT_BLACK;

        // CAS — only succeeds if game is still PLAYING
        if (state.compareAndSet(GameState.PLAYING, timeoutState)) {
            eventLog.add("TIMEOUT: " + timedOutPlayer + " ran out of time");
            return true;
        }
        eventLog.add("TIMEOUT REJECTED: game already ended as " + state.get());
        return false;
    }

    public GameState getState() { return state.get(); }
    public List<String> getEventLog() { return eventLog; }
    public String getCurrentPlayer() { return currentPlayer; }
    public Board getBoard() { return board; }
}
