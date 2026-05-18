/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the optimized blackjack game
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Blackjack Demo (Optimized - Memoized + Fisher-Yates + Basic-Strategy Chart) ===");

        // ---- Game A: Standard dealer (hit < 17) -----------------------------------------------
        System.out.println("\n############ Game A: StandardDealerStrategy ############");
        Player playerA = new Player("Player-A", 1000);
        BlackjackGame gameA = new BlackjackGame(playerA, new StandardDealerStrategy(), 42L);
        playTwoRounds(gameA, playerA);
        System.out.println("\nGame A final balance: $" + String.format("%.0f", playerA.getBalance()));

        // ---- Game B: Basic-strategy chart dealer ----------------------------------------------
        System.out.println("\n############ Game B: BasicStrategyChartDealer ############");
        Player playerB = new Player("Player-B", 1000);
        BlackjackGame gameB = new BlackjackGame(playerB, new BasicStrategyChartDealer(), 42L);
        playTwoRounds(gameB, playerB);
        System.out.println("\nGame B final balance: $" + String.format("%.0f", playerB.getBalance()));

        // ---- Direct chart lookup demonstration -------------------------------------------------
        System.out.println("\n--- Direct Basic-Strategy Chart Lookups ---");
        System.out.println("Player 16 vs dealer 10 -> "
            + BasicStrategyChartDealer.lookup(16, 10) + "  (classic hit-and-pray)");
        System.out.println("Player 12 vs dealer  4 -> "
            + BasicStrategyChartDealer.lookup(12,  4) + "  (let the dealer bust)");
        System.out.println("Player 11 vs dealer  6 -> "
            + BasicStrategyChartDealer.lookup(11,  6) + "  (textbook double)");
        System.out.println("Player 19 vs dealer  9 -> "
            + BasicStrategyChartDealer.lookup(19,  9) + "  (always stand on 19)");

        System.out.println("\n=== Blackjack Demo Complete ===");
    }

    private static void playTwoRounds(BlackjackGame game, Player player) {
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
    }
}
