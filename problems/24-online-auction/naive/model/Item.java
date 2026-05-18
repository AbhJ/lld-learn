/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Item.java — Auction item with reserve price
public class Item {
    private String id;                                    // private = encapsulates item identity
    private String name;
    private String description;
    private double reservePrice;                          // private = minimum price to sell; hidden from bidders

    public Item(String id, String name, String description, double reservePrice) {
        this.id = id; this.name = name; this.description = description; this.reservePrice = reservePrice;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getReservePrice() { return reservePrice; }
    @Override public String toString() { return name; }
}
