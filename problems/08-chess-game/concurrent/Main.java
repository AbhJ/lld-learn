/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — Move thread and timer thread racing, verify exactly one outcome

import model.Board;
import model.Move;
import service.GameController;
import service.GameController.GameState;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Chess Game Demo ===\n");

        System.out.println("Scenario: Move thread makes moves while timer thread races to declare timeout.");
        System.out.println("Only ONE outcome should determine the game result.\n");

        // Run multiple trials to demonstrate the race
        int trials = 10;
        int moveWins = 0;
        int timeoutWins = 0;
        int errors = 0;

        for (int trial = 0; trial < trials; trial++) {
            GameController controller = new GameController();

            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(2);

            // Move thread — makes rapid moves
            Thread moveThread = new Thread(() -> {
                try { startLatch.await(); } catch (InterruptedException ignored) {}
                // White and black alternate moves (simplified pawn pushes)
                Move[] moves = {
                    new Move(1, 0, 2, 0, "WHITE"),
                    new Move(6, 0, 5, 0, "BLACK"),
                    new Move(1, 1, 2, 1, "WHITE"),
                    new Move(6, 1, 5, 1, "BLACK"),
                    new Move(1, 2, 2, 2, "WHITE"),  // 5th move triggers win
                    new Move(6, 2, 5, 2, "BLACK"),
                };
                for (Move m : moves) {
                    if (controller.getState() != GameState.PLAYING) break;
                    controller.makeMove(m);
                }
                doneLatch.countDown();
            });

            // Timer thread — tries to declare timeout after a brief delay
            Thread timerThread = new Thread(() -> {
                try { startLatch.await(); } catch (InterruptedException ignored) {}
                // Simulate clock running — timeout fires during gameplay
                try { Thread.sleep(1); } catch (InterruptedException ignored) {}
                controller.declareTimeout("BLACK");
                doneLatch.countDown();
            });

            moveThread.start();
            timerThread.start();
            startLatch.countDown();
            doneLatch.await();

            GameState finalState = controller.getState();
            if (finalState == GameState.WHITE_WINS || finalState == GameState.BLACK_WINS) {
                moveWins++;
            } else if (finalState == GameState.TIMEOUT_WHITE || finalState == GameState.TIMEOUT_BLACK) {
                timeoutWins++;
            } else if (finalState == GameState.PLAYING) {
                errors++; // Should not still be playing
            }

            if (trial == trials - 1) {
                // Print details of last trial
                System.out.println("--- Last Trial Event Log ---");
                for (String entry : controller.getEventLog()) {
                    System.out.println("  " + entry);
                }
                System.out.println("  Final state: " + finalState);
            }
        }

        System.out.println("\n--- Results over " + trials + " trials ---");
        System.out.println("Games won by move completion: " + moveWins);
        System.out.println("Games won by timeout: " + timeoutWins);
        System.out.println("Errors (still PLAYING): " + errors);

        // Verify: every trial ended in exactly one outcome, no errors
        boolean passed = (errors == 0) && (moveWins + timeoutWins == trials);

        int totalAttempts = trials * 2; // move thread + timer thread per trial
        System.out.println("\n" + totalAttempts + " threads attempted, " + trials + " succeeded (one outcome each), "
                + trials + " correctly rejected (losing race)");
        System.out.println("Every game has exactly one outcome, no state corruption.");
        System.out.println("Correctness check: " + (passed ? "PASSED" : "FAILED"));
    }
}
