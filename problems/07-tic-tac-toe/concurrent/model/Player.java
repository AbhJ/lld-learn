/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Player.java — Player with assigned symbol

package model;

public class Player {
    private final String name;          // final = immutable; safe to share across threads
    private final Board.Symbol symbol;  // final = symbol never changes; safe publication

    public Player(String name, Board.Symbol symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public String getName() { return name; }
    public Board.Symbol getSymbol() { return symbol; }

    @Override
    public String toString() {
        return name + "(" + symbol + ")";
    }
}
