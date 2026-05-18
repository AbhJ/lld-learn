/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — Rapid-fire moves from 2 threads, verify no cell overwritten

import model.Board;
import model.Board.Symbol;
import model.Player;
import service.Game;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Tic-Tac-Toe Demo ===\n");

        Board board = new Board(3);
        Player playerX = new Player("Alice", Symbol.X);
        Player playerO = new Player("Bob", Symbol.O);
        Game game = new Game(board, playerX, playerO);

        System.out.println("Players: " + playerX + " vs " + playerO);
        System.out.println("Both threads fire moves rapidly at all cells.\n");

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        // Player X thread — tries all cells rapidly
        Thread threadX = new Thread(() -> {
            try { startLatch.await(); } catch (InterruptedException ignored) {}
            for (int attempt = 0; attempt < 100 && !game.isGameOver(); attempt++) {
                int row = ThreadLocalRandom.current().nextInt(3);
                int col = ThreadLocalRandom.current().nextInt(3);
                game.attemptMove(playerX, row, col);
                Thread.yield();
            }
            doneLatch.countDown();
        });

        // Player O thread — tries all cells rapidly
        Thread threadO = new Thread(() -> {
            try { startLatch.await(); } catch (InterruptedException ignored) {}
            for (int attempt = 0; attempt < 100 && !game.isGameOver(); attempt++) {
                int row = ThreadLocalRandom.current().nextInt(3);
                int col = ThreadLocalRandom.current().nextInt(3);
                game.attemptMove(playerO, row, col);
                Thread.yield();
            }
            doneLatch.countDown();
        });

        threadX.start();
        threadO.start();
        startLatch.countDown();
        doneLatch.await();

        // Print results
        System.out.println("--- Results ---");
        System.out.println("Final board:");
        System.out.println(board);
        System.out.println("Game result: " + game.getResult());

        System.out.println("Move log:");
        for (String entry : game.getMoveLog()) {
            System.out.println("  " + entry);
        }

        // Verify no cell was overwritten
        boolean noOverwrite = verifyNoOverwrite(game);
        int totalAttempts = game.getSuccessfulMoves() + game.getRejectedMoves();

        System.out.println("\n" + totalAttempts + " threads attempted, " + game.getSuccessfulMoves()
                + " succeeded, " + game.getRejectedMoves() + " correctly rejected");
        System.out.println("No cell overwritten: " + noOverwrite);
        System.out.println("Correctness check: " + (noOverwrite ? "PASSED" : "FAILED"));
    }

    private static boolean verifyNoOverwrite(Game game) {
        // Each cell should have been placed at most once in the move log
        boolean[][] placed = new boolean[3][3];
        for (String entry : game.getMoveLog()) {
            // Parse "(row,col)" from log
            int openParen = entry.lastIndexOf('(');
            int comma = entry.indexOf(',', openParen);
            int closeParen = entry.indexOf(')', comma);
            int row = Integer.parseInt(entry.substring(openParen + 1, comma));
            int col = Integer.parseInt(entry.substring(comma + 1, closeParen));
            if (placed[row][col]) {
                return false; // overwritten!
            }
            placed[row][col] = true;
        }
        return true;
    }
}
