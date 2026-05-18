/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating tic-tac-toe with test scenarios

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Tic Tac Toe Test ===\n");

        // Test 1: X wins by row
        System.out.println("--- Test 1: X Wins by Row ---");
        int[][] xMoves1 = {{0,0}, {0,1}, {0,2}};
        int[][] oMoves1 = {{1,0}, {1,1}};
        Player p1 = new HumanPlayer("Alice", Symbol.X, xMoves1);
        Player p2 = new HumanPlayer("Bob", Symbol.O, oMoves1);
        Game game1 = new Game(3, p1, p2);
        game1.playFullGame(true);
        System.out.println(game1.getBoard().display());
        System.out.println("Result: " + game1.getStatusMessage());

        // Test 2: O wins by column
        System.out.println("\n--- Test 2: O Wins by Column ---");
        int[][] xMoves2 = {{0,0}, {1,0}, {2,2}};
        int[][] oMoves2 = {{0,1}, {1,1}, {2,1}};
        Game game2 = new Game(3, new HumanPlayer("Alice", Symbol.X, xMoves2), new HumanPlayer("Bob", Symbol.O, oMoves2));
        game2.playFullGame(true);
        System.out.println(game2.getBoard().display());
        System.out.println("Result: " + game2.getStatusMessage());

        // Test 3: X wins by diagonal
        System.out.println("\n--- Test 3: X Wins by Diagonal ---");
        int[][] xMoves3 = {{0,0}, {1,1}, {2,2}};
        int[][] oMoves3 = {{0,1}, {0,2}};
        Game game3 = new Game(3, new HumanPlayer("Alice", Symbol.X, xMoves3), new HumanPlayer("Bob", Symbol.O, oMoves3));
        game3.playFullGame(true);
        System.out.println(game3.getBoard().display());
        System.out.println("Result: " + game3.getStatusMessage());

        // Test 4: Draw
        System.out.println("\n--- Test 4: Draw ---");
        int[][] xMoves4 = {{0,0}, {0,2}, {1,0}, {1,1}, {2,1}};
        int[][] oMoves4 = {{0,1}, {1,2}, {2,0}, {2,2}};
        Game game4 = new Game(3, new HumanPlayer("Alice", Symbol.X, xMoves4), new HumanPlayer("Bob", Symbol.O, oMoves4));
        game4.playFullGame(false);
        System.out.println(game4.getBoard().display());
        System.out.println("Result: " + game4.getStatusMessage());

        // Test 5: Undo
        System.out.println("\n--- Test 5: Undo Move ---");
        int[][] xMoves5 = {{1,1}, {0,0}};
        int[][] oMoves5 = {{0,1}};
        Game game5 = new Game(3, new HumanPlayer("Alice", Symbol.X, xMoves5), new HumanPlayer("Bob", Symbol.O, oMoves5));
        game5.playTurn();
        System.out.println("After X at (1,1):");
        System.out.println(game5.getBoard().display());
        game5.undoLastMove();
        System.out.println("After undo:");
        System.out.println(game5.getBoard().display());

        // Test 6: AI vs AI with Observer hooked up
        System.out.println("--- Test 6: AI vs AI (with observer) ---");
        Game game6 = new Game(3, new AIPlayer("AI-X", Symbol.X, 42), new AIPlayer("AI-O", Symbol.O, 123));
        // Observer: console logger receives move/win/draw events.
        game6.addObserver(new ConsoleGameObserver());
        game6.playFullGame(true);
        System.out.println(game6.getBoard().display());
        System.out.println("Result: " + game6.getStatusMessage());

        System.out.println("\n=== All Tests Passed ===");
    }
}
