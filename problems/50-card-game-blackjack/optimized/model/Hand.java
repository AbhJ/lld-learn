/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Hand.java — Hand with memoized score computation
import java.util.ArrayList;
import java.util.List;

public class Hand {
    private List<Card> cards;   // ArrayList = dynamic card collection for the hand
    // Memoized score — only recomputes when hand changes, not on every getScore()
    private int cachedScore;    // cached value = O(1) repeat access without re-summing
    private boolean scoreDirty; // dirty flag = signals when cache must be recomputed

    public Hand() { this.cards = new ArrayList<>(); this.cachedScore = 0; this.scoreDirty = true; }

    public void addCard(Card card) {
        cards.add(card);
        scoreDirty = true; // Invalidate cache
    }

    public List<Card> getCards() { return new ArrayList<>(cards); }

    // WHY: O(1) when cache is valid (most calls), O(n) only after addCard
    public int getScore() {
        if (!scoreDirty) return cachedScore;
        int score = 0; int aces = 0;
        for (Card card : cards) { score += card.getValue(); if (card.isAce()) aces++; }
        while (score > 21 && aces > 0) { score -= 10; aces--; }
        cachedScore = score;
        scoreDirty = false;
        return cachedScore;
    }

    public boolean isBusted() { return getScore() > 21; }
    public boolean isBlackjack() { return cards.size() == 2 && getScore() == 21; }
    public int size() { return cards.size(); }

    public void clear() { cards.clear(); cachedScore = 0; scoreDirty = true; }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < cards.size(); i++) { if (i > 0) sb.append(", "); sb.append(cards.get(i)); }
        return sb.append("]").toString();
    }
}
