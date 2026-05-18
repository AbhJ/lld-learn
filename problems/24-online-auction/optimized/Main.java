/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates auction with max-heap and CAS-based bid validation
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Online Auction Demo (Optimized) ===");
        System.out.println("Optimizations: Max-heap for O(1) highest bid, CAS for concurrent validation\n");

        AuctionSystem system = new AuctionSystem();
        Bidder alice = system.registerBidder("b1", "Alice");
        Bidder bob = system.registerBidder("b2", "Bob");

        Item watch = new Item("i1", "Vintage Watch", "1960s Swiss", 100.0);
        Auction auction = system.createAuction("a1", watch, 100.0, 10.0, new WinnerStrategy.HighestBidStrategy());
        auction.start();

        System.out.println("--- Placing Bids (O(1) highest via max-heap) ---");
        auction.placeBid(alice, 150.0);
        System.out.println("Alice bids $150 → highest: $" + String.format("%.2f", auction.getCurrentHighestBid()));
        auction.placeBid(bob, 200.0);
        System.out.println("Bob bids $200 → highest: $" + String.format("%.2f", auction.getCurrentHighestBid()));
        auction.placeBid(alice, 250.0);
        System.out.println("Alice bids $250 → highest: $" + String.format("%.2f", auction.getCurrentHighestBid()));

        System.out.println("\n--- Closing Auction ---");
        WinnerStrategy.WinnerResult result = auction.close();
        if (result != null) System.out.println("Winner: " + result.getWinner() + " at $" + String.format("%.2f", result.getWinningPrice()));

        System.out.println("\n=== Demo Complete ===");
    }
}
