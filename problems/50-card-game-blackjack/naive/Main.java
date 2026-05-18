/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the blackjack game
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Blackjack Demo (Naive) ===");
        Player player = new Player("Player", 1000);
        BlackjackGame game = new BlackjackGame(player, new StandardDealerStrategy(), 42L);

        System.out.println("\n--- Round 1 ---");
        game.placeBet(100); game.deal();
        if (game.getState() == GameState.PLAYING) {
            if (player.getHand().getScore() < 17) game.hit();
            if (game.getState() == GameState.PLAYING) game.stand();
        }

        game.newRound();
        System.out.println("\n--- Round 2 ---");
        game.placeBet(150); game.deal();
        if (game.getState() == GameState.PLAYING) {
            if (player.getHand().getScore() < 17) game.hit();
            if (game.getState() == GameState.PLAYING) game.stand();
        }

        System.out.println("\nFinal balance: $" + String.format("%.0f", player.getBalance()));
        System.out.println("\n=== Blackjack Demo Complete ===");
    }
}
