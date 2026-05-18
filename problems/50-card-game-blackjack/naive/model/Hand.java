/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Hand.java — Represents a player's current hand of cards
import java.util.ArrayList;
import java.util.List;

public class Hand {
    private List<Card> cards; // private = hand managed via addCard()/clear()

    public Hand() { this.cards = new ArrayList<>(); }
    public void addCard(Card card) { cards.add(card); }
    public List<Card> getCards() { return new ArrayList<>(cards); }

    public int getScore() {
        int score = 0; int aces = 0;
        for (Card card : cards) { score += card.getValue(); if (card.isAce()) aces++; }
        while (score > 21 && aces > 0) { score -= 10; aces--; }
        return score;
    }

    public boolean isBusted() { return getScore() > 21; }
    public boolean isBlackjack() { return cards.size() == 2 && getScore() == 21; }
    public void clear() { cards.clear(); }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < cards.size(); i++) { if (i > 0) sb.append(", "); sb.append(cards.get(i)); }
        return sb.append("]").toString();
    }
}
