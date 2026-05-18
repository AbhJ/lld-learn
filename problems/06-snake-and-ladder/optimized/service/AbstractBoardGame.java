/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/AbstractBoardGame.java — Template Method for turn-based board games
//
// playTurn() is the *invariant* skeleton:
//   1. roll the dice
//   2. ask the subclass to compute / apply the move (applyMove)
//   3. notify the subclass before/after the move (onBeforeMove / onAfterMove)
//   4. ask the subclass whether the current player has won (checkVictory)
// Subclasses customise individual steps without changing the flow.

import java.util.ArrayList;
import java.util.List;

abstract class AbstractBoardGame {
    protected final List<Player> players = new ArrayList<>();
    protected final Dice dice;
    protected int currentPlayerIndex = 0;
    protected Player winner = null;
    protected int turnCount = 0;
    protected boolean verbose;

    protected AbstractBoardGame(Dice dice, boolean verbose) {
        this.dice = dice;
        this.verbose = verbose;
    }

    public void addPlayer(String name) { players.add(new Player(name)); }

    /**
     * Template method: defines the invariant turn flow. Final so subclasses
     * cannot reorder or skip steps. Returns true if the game should continue.
     */
    public final boolean playTurn() {
        if (winner != null) return false;
        Player current = players.get(currentPlayerIndex);
        turnCount++;
        int roll = dice.roll();

        onBeforeMove(current, roll);                    // hook
        int oldPosition = current.getPosition();
        int landedPosition = applyMove(current, roll);  // primitive op
        onAfterMove(current, roll, oldPosition, landedPosition); // hook

        if (checkVictory(current)) {
            winner = current;
            current.setWon(true);
            onVictory(current);
            return false;
        }
        advanceTurn();
        return true;
    }

    public void play(int maxTurns) {
        while (winner == null && turnCount < maxTurns) {
            playTurn();
        }
        if (winner == null && verbose) {
            System.out.println("  Game ended after " + maxTurns + " turns with no winner.");
        }
    }

    protected void advanceTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    // === Primitive operations (subclass must implement) ===

    /**
     * Apply the dice roll to the current player and return the position they
     * actually landed on (after any board effects like snakes/ladders).
     */
    protected abstract int applyMove(Player current, int roll);

    /** Did the current player just win? */
    protected abstract boolean checkVictory(Player current);

    // === Hooks (subclass may override; default no-ops) ===

    protected void onBeforeMove(Player current, int roll) { }
    protected void onAfterMove(Player current, int roll, int oldPosition, int landedPosition) { }
    protected void onVictory(Player winner) { }

    // === Read-only accessors ===

    public Player getWinner() { return winner; }
    public int getTurnCount() { return turnCount; }
    public List<Player> getPlayers() { return players; }
}
