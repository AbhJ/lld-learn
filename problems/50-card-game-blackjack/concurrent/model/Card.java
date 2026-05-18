/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Card.java — Immutable card representation

public class Card {
    private final String suit;  // final = immutable; safe to share across threads without sync
    private final String rank;  // final = immutable; no thread can corrupt this value
    private final int value;    // final = card value never changes; safe publication guaranteed

    public Card(String suit, String rank, int value) {
        this.suit = suit;
        this.rank = rank;
        this.value = value;
    }

    public String getSuit() { return suit; }
    public String getRank() { return rank; }
    public int getValue() { return value; }

    @Override
    public String toString() { return rank + " of " + suit; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        Card c = (Card) o;
        return suit.equals(c.suit) && rank.equals(c.rank);
    }

    @Override
    public int hashCode() { return suit.hashCode() * 31 + rank.hashCode(); }
}
