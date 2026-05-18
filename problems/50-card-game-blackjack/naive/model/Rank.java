/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Rank.java — Enumerates card ranks with values
public enum Rank { // enum = fixed set of card ranks with point values
    ACE("Ace",11), TWO("2",2), THREE("3",3), FOUR("4",4), FIVE("5",5),
    SIX("6",6), SEVEN("7",7), EIGHT("8",8), NINE("9",9), TEN("10",10),
    JACK("Jack",10), QUEEN("Queen",10), KING("King",10);
    private String displayName; private int value; // private = name/value per rank constant
    Rank(String displayName, int value) { this.displayName = displayName; this.value = value; }
    public int getValue() { return value; }
    @Override public String toString() { return displayName; }
}
