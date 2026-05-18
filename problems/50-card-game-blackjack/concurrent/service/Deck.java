/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/Deck.java — Thread-safe deck with synchronized dealing and AtomicInteger position

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Deck {
    private final List<Card> cards;              // final = card list reference never changes; contents fixed after init
    private final AtomicInteger position;        // AtomicInteger = thread-safe deck pointer; tracks dealt count
    private final Object dealLock = new Object(); // synchronized on this lock = prevents two threads getting same card

    public Deck() {
        this.cards = new ArrayList<>();
        this.position = new AtomicInteger(0);
        initializeDeck();
        Collections.shuffle(cards);
    }

    private void initializeDeck() {
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        int[] values = {2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10, 11};

        for (String suit : suits) {
            for (int i = 0; i < ranks.length; i++) {
                cards.add(new Card(suit, ranks[i], values[i]));
            }
        }
    }

    /**
     * Deal a card. Synchronized to prevent two players getting the same card.
     * Uses CAS on position to atomically advance the deck pointer.
     * Returns null if deck is exhausted.
     */
    public Card deal() {
        synchronized (dealLock) {
            int idx = position.get();
            if (idx >= cards.size()) {
                return null; // Deck exhausted
            }
            Card card = cards.get(idx);
            position.incrementAndGet();
            return card;
        }
    }

    public int getCardsDealt() { return position.get(); }
    public int getCardsRemaining() { return cards.size() - position.get(); }
    public int getTotalCards() { return cards.size(); }
}
