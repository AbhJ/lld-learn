/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 50 rapid bids, verify final winner is actually the highest bidder

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Online Auction Demo ===\n");

        AuctionService auction = new AuctionService();
        int bidderCount = 50;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(bidderCount);

        // Each bidder bids a unique amount so we can verify the winner
        // Bidder i bids (i+1) * 100 cents = $1 to $50
        long[] bidAmounts = new long[bidderCount];
        for (int i = 0; i < bidderCount; i++) {
            bidAmounts[i] = (i + 1) * 100L;
        }
        // Shuffle so submission order is random
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < bidderCount; i++) indices.add(i);
        Collections.shuffle(indices, new Random(42));

        System.out.println("Scenario: 50 bidders submit bids ($1 to $50) simultaneously.");
        System.out.println("Expected: Winner is always the $50 bidder (highest amount).\n");

        for (int t = 0; t < bidderCount; t++) {
            final int idx = indices.get(t);
            new Thread(() -> {
                try {
                    startLatch.await();
                    Bid bid = new Bid("Bidder-" + idx, bidAmounts[idx], System.nanoTime());
                    auction.placeBid(bid);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }, "Bidder-" + idx).start();
        }

        startLatch.countDown();
        doneLatch.await();

        Bid winner = auction.getCurrentHighest();
        long expectedHighest = bidderCount * 100L; // $50.00

        System.out.println("--- Results ---");
        System.out.println("Total bids attempted: " + auction.getTotalBidsAttempted());
        System.out.println("Successful bid updates: " + auction.getSuccessfulBids());
        System.out.println("Winning bid: " + winner);
        System.out.println("Expected highest: $" + (expectedHighest / 100) + ".00");

        boolean passed = winner != null && winner.getAmountCents() == expectedHighest;
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}
