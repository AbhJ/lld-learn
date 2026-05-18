/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Card.java — Represents a single playing card
public class Card {
    private Rank rank; // private = card rank encapsulated; accessed via getRank()
    private Suit suit; // private = card suit encapsulated; accessed via getSuit()

    public Card(Rank rank, Suit suit) { this.rank = rank; this.suit = suit; }
    public Rank getRank() { return rank; }
    public Suit getSuit() { return suit; }
    public int getValue() { return rank.getValue(); }
    public boolean isAce() { return rank == Rank.ACE; }
    @Override public String toString() { return rank + " of " + suit; }
}
