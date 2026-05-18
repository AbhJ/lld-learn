/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Deck.java — Deck with Fisher-Yates in-place shuffle (no new object allocation)
import java.util.Random;

public class Deck {
    // Fixed-size array avoids ArrayList overhead and GC pressure
    private Card[] cards;                   // Card[] = fixed array; no resizing, no boxing overhead
    private int top;                        // index of next card to deal; O(1) dealing
    private Random random;                  // seeded RNG for reproducible shuffles
    private static final int DECK_SIZE = 52; // static final = constant; same for all Deck instances

    public Deck(long seed) {
        this.random = new Random(seed);
        this.cards = new Card[DECK_SIZE];
        buildDeck();
        shuffle();
    }

    private void buildDeck() {
        int i = 0;
        for (Suit suit : Suit.values())
            for (Rank rank : Rank.values())
                cards[i++] = new Card(rank, suit);
        top = DECK_SIZE - 1;
    }

    // WHY: Fisher-Yates in-place shuffle — O(n) with no new allocations
    // vs Collections.shuffle which may create temporary arrays
    public void shuffle() {
        for (int i = DECK_SIZE - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Card temp = cards[i];
            cards[i] = cards[j];
            cards[j] = temp;
        }
        top = DECK_SIZE - 1;
    }

    public Card deal() {
        if (top < 0) { shuffle(); }
        return cards[top--];
    }

    public int remaining() { return top + 1; }

    // WHY: Reset just reshuffles existing cards in-place — no GC
    public void reset() { shuffle(); }
}
