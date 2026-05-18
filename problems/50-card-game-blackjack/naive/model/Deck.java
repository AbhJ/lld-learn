/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Deck.java — 52-card deck with shuffle (creates new deck on reshuffle)
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Deck {
    private List<Card> cards;  // private = deck contents managed internally
    private Random random;     // private = seed-based RNG for reproducible shuffles

    public Deck(long seed) { this.random = new Random(seed); buildDeck(); }

    private void buildDeck() {
        cards = new ArrayList<>();
        for (Suit suit : Suit.values())
            for (Rank rank : Rank.values())
                cards.add(new Card(rank, suit));
    }

    // Creates new deck objects on every shuffle
    public void shuffle() { Collections.shuffle(cards, random); }

    public Card deal() {
        if (cards.isEmpty()) { buildDeck(); shuffle(); }
        return cards.remove(cards.size() - 1);
    }

    public int remaining() { return cards.size(); }
    public void reset() { buildDeck(); shuffle(); }
}
