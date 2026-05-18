/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 4 players rapidly sending move requests, verify strict turn order

import model.Board;
import model.Player;
import service.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Snake and Ladder Demo ===\n");

        final int NUM_PLAYERS = 4;
        final int MOVES_PER_PLAYER = 50;

        // Setup board
        Board board = new Board(100);
        board.addSnake(97, 78);
        board.addSnake(62, 19);
        board.addSnake(46, 5);
        board.addLadder(3, 38);
        board.addLadder(8, 30);
        board.addLadder(28, 74);

        List<Player> players = new ArrayList<>();
        for (int i = 0; i < NUM_PLAYERS; i++) {
            players.add(new Player(i, "Player-" + i));
        }

        Game game = new Game(board, players);

        System.out.println("Board size: 100, Players: " + NUM_PLAYERS);
        System.out.println("Each player sends " + MOVES_PER_PLAYER + " move requests rapidly.\n");

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(NUM_PLAYERS);

        // Each player thread rapidly tries to make moves
        for (Player player : players) {
            new Thread(() -> {
                try {
                    startLatch.await();
                } catch (InterruptedException ignored) {}

                for (int i = 0; i < MOVES_PER_PLAYER; i++) {
                    if (game.isGameOver()) break;
                    game.attemptMove(player);
                    // Small yield to let other threads compete
                    Thread.yield();
                }
                doneLatch.countDown();
            }).start();
        }

        // Release all threads
        startLatch.countDown();
        doneLatch.await();

        // Verify results
        System.out.println("--- Results ---");
        System.out.println("Valid moves executed: " + game.getValidMoves());
        System.out.println("Out-of-turn moves rejected: " + game.getRejectedMoves());

        System.out.println("\nMove log (last 15 entries):");
        List<String> log = game.getMoveLog();
        int start = Math.max(0, log.size() - 15);
        for (int i = start; i < log.size(); i++) {
            System.out.println("  " + log.get(i));
        }

        // Verify strict turn order from log
        boolean turnOrderCorrect = verifyTurnOrder(game.getMoveLog(), NUM_PLAYERS);

        System.out.println("\nFinal positions:");
        for (Player p : players) {
            System.out.println("  " + p);
        }

        int totalAttempts = game.getValidMoves() + game.getRejectedMoves();
        System.out.println("\n" + totalAttempts + " threads attempted, " + game.getValidMoves()
                + " succeeded, " + game.getRejectedMoves() + " correctly rejected");
        System.out.println("Turn order strictly enforced: " + turnOrderCorrect);
        System.out.println("Correctness check: " + (turnOrderCorrect ? "PASSED" : "FAILED"));
    }

    private static boolean verifyTurnOrder(List<String> moveLog, int numPlayers) {
        int expectedPlayer = 0;
        for (String entry : moveLog) {
            if (entry.contains("WINS")) continue;
            // Extract player number from "Player-X rolled..."
            if (entry.startsWith("Player-")) {
                int dashIdx = entry.indexOf('-');
                int spaceIdx = entry.indexOf(' ', dashIdx);
                int playerId = Integer.parseInt(entry.substring(dashIdx + 1, spaceIdx));
                if (playerId != expectedPlayer) {
                    return false;
                }
                expectedPlayer = (expectedPlayer + 1) % numPlayers;
            }
        }
        return true;
    }
}
