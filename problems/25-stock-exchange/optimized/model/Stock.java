/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Stock.java — A listed stock with symbol
public class Stock {
    private String symbol;                                // used as HashMap key for O(1) order book lookup
    private String name;
    public Stock(String symbol, String name) { this.symbol = symbol; this.name = name; }
    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    @Override public boolean equals(Object o) { return o instanceof Stock && symbol.equals(((Stock) o).symbol); }
    @Override public int hashCode() { return symbol.hashCode(); }
    @Override public String toString() { return symbol; }
}
