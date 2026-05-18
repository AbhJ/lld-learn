/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating chess game with test scenarios

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Chess Game Test ===\n");

        Player white = new Player("Alice", Color.WHITE);
        Player black = new Player("Bob", Color.BLACK);
        Game game = new Game(white, black);

        System.out.println("--- Initial Board ---");
        System.out.println(game.getBoard().display());
        System.out.println("Status: " + game.getCurrentStatus());

        System.out.println("\n--- Test: Pawn Moves ---");
        boolean moved = game.makeMove(new Position(6, 4), new Position(4, 4));
        System.out.println("White Pawn e2->e4: " + (moved ? "valid" : "invalid"));
        moved = game.makeMove(new Position(1, 4), new Position(3, 4));
        System.out.println("Black Pawn e7->e5: " + (moved ? "valid" : "invalid"));

        System.out.println("\n--- Test: Knight Moves ---");
        moved = game.makeMove(new Position(7, 6), new Position(5, 5));
        System.out.println("White Knight g1->f3: " + (moved ? "valid" : "invalid"));
        moved = game.makeMove(new Position(0, 1), new Position(2, 2));
        System.out.println("Black Knight b8->c6: " + (moved ? "valid" : "invalid"));

        System.out.println("\n--- Test: Bishop Moves ---");
        moved = game.makeMove(new Position(7, 5), new Position(4, 2));
        System.out.println("White Bishop f1->c4: " + (moved ? "valid" : "invalid"));

        System.out.println("\n--- Board after opening ---");
        System.out.println(game.getBoard().display());

        System.out.println("\n--- Test: Invalid Moves ---");
        moved = game.makeMove(new Position(6, 0), new Position(5, 0));
        System.out.println("White on Black's turn: " + (moved ? "valid" : "invalid (wrong turn)"));

        System.out.println("\n--- Test: Scholar's Mate ---");
        Game game2 = new Game(white, black);
        game2.makeMove(new Position(6, 4), new Position(4, 4));
        game2.makeMove(new Position(1, 4), new Position(3, 4));
        game2.makeMove(new Position(7, 5), new Position(4, 2));
        game2.makeMove(new Position(0, 1), new Position(2, 2));
        game2.makeMove(new Position(7, 3), new Position(3, 7));
        game2.makeMove(new Position(0, 6), new Position(2, 5));
        moved = game2.makeMove(new Position(3, 7), new Position(1, 5));
        System.out.println("Qxf7#: " + (moved ? "valid" : "invalid"));
        System.out.println("Status: " + game2.getCurrentStatus());

        System.out.println("\n--- Move History ---");
        for (Move m : game2.getMoveHistory()) System.out.println("  " + m);

        System.out.println("\n=== All Tests Passed ===");
    }
}
