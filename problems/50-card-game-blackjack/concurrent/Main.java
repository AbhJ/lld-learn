/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 7 players hitting simultaneously from shared deck, verify no duplicate cards dealt

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Blackjack Card Game Demo ===\n");
        System.out.println("Race condition: Multiple players hitting simultaneously");
        System.out.println("— deck deals same card twice.\n");

        Deck deck = new Deck();
        int playerCount = 7;
        int hitsPerPlayer = 5; // Each player hits 5 times

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(playerCount);

        // Collect all dealt cards per player
        ConcurrentHashMap<String, List<Card>> playerHands = new ConcurrentHashMap<>();
        List<Card> allDealtCards = new CopyOnWriteArrayList<>();
        AtomicInteger nullDeals = new AtomicInteger(0);

        for (int p = 0; p < playerCount; p++) {
            final String playerId = "Player-" + p;
            playerHands.put(playerId, new CopyOnWriteArrayList<>());

            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int h = 0; h < hitsPerPlayer; h++) {
                        Card card = deck.deal();
                        if (card != null) {
                            playerHands.get(playerId).add(card);
                            allDealtCards.add(card);
                        } else {
                            nullDeals.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await();

        // Verify: no duplicate cards
        Set<String> cardSet = new HashSet<>();
        boolean noDuplicates = true;
        String duplicateCard = null;
        for (Card card : allDealtCards) {
            String key = card.getRank() + "-" + card.getSuit();
            if (!cardSet.add(key)) {
                noDuplicates = false;
                duplicateCard = key;
                break;
            }
        }

        int totalDealt = allDealtCards.size();
        int expectedDealt = playerCount * hitsPerPlayer; // 35 cards max

        // Verify deck position matches cards dealt
        boolean positionCorrect = (deck.getCardsDealt() == totalDealt);

        // Verify each player got cards
        boolean allPlayersGotCards = true;
        for (int p = 0; p < playerCount; p++) {
            if (playerHands.get("Player-" + p).isEmpty()) {
                allPlayersGotCards = false;
                break;
            }
        }

        System.out.println("--- Results ---");
        System.out.println("Players: " + playerCount);
        System.out.println("Hits per player: " + hitsPerPlayer);
        System.out.println("Total cards dealt: " + totalDealt);
        System.out.println("Expected deals: " + expectedDealt);
        System.out.println("Deck cards remaining: " + deck.getCardsRemaining());
        System.out.println("Null deals (deck exhausted): " + nullDeals.get());

        System.out.println("\nPlayer hands:");
        for (int p = 0; p < playerCount; p++) {
            String pid = "Player-" + p;
            List<Card> hand = playerHands.get(pid);
            System.out.println("  " + pid + " (" + hand.size() + " cards): " + hand);
        }

        System.out.println("\n--- Consistency Checks ---");
        System.out.println("No duplicate cards: " + noDuplicates);
        if (!noDuplicates) System.out.println("  DUPLICATE: " + duplicateCard);
        System.out.println("Deck position matches dealt: " + positionCorrect);
        System.out.println("All players got cards: " + allPlayersGotCards);
        System.out.println("Total accounted (dealt + remaining + null): " +
                (totalDealt + deck.getCardsRemaining()) + "/" + deck.getTotalCards());

        boolean passed = noDuplicates && positionCorrect && allPlayersGotCards;
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}
