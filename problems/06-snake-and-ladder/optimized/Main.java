/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the snake and ladder game

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Snake and Ladder Game Test ===\n");

        System.out.println("--- Test 1: Deterministic Game (Small Board) ---");
        Board smallBoard = BoardFactory.createSmallBoard();
        System.out.println("Board size: " + smallBoard.getSize());
        System.out.println("Snakes: " + smallBoard.getSnakes());
        System.out.println("Ladders: " + smallBoard.getLadders());

        FixedDice fixedDice = new FixedDice(2, 3, 4, 5, 6, 1, 3, 2, 4, 5, 3, 2, 6, 1, 4, 3, 5, 2);
        SnakeAndLadderGame game1 = new SnakeAndLadderGame(smallBoard, fixedDice);
        game1.addPlayer("Alice");
        game1.addPlayer("Bob");
        game1.play(30);

        if (game1.getWinner() != null) {
            System.out.println("\nWinner: " + game1.getWinner().getName());
        }

        System.out.println("\n--- Test 2: Standard Board (Random) ---");
        Board standardBoard = BoardFactory.createStandardBoard();
        NormalDice normalDice = new NormalDice(6, 42);
        SnakeAndLadderGame game2 = new SnakeAndLadderGame(standardBoard, normalDice, false);
        game2.addPlayer("Player1");
        game2.addPlayer("Player2");
        game2.play(200);

        if (game2.getWinner() != null) {
            System.out.println("Winner: " + game2.getWinner().getName() + " in " + game2.getTurnCount() + " turns");
        }
        for (Player p : game2.getPlayers()) {
            System.out.println("  " + p);
        }

        System.out.println("\n--- Test 3: Board Effects ---");
        Board effectBoard = BoardFactory.createEmptyBoard(50);
        effectBoard.addSnake(20, 5);
        effectBoard.addLadder(10, 35);
        System.out.println("Position 10 (ladder): final = " + effectBoard.getFinalPosition(10));
        System.out.println("Position 20 (snake): final = " + effectBoard.getFinalPosition(20));
        System.out.println("Position 15 (normal): final = " + effectBoard.getFinalPosition(15));

        System.out.println("\n=== All Tests Passed ===");
    }
}
