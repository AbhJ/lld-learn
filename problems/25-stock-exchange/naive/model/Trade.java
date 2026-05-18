/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Trade.java — Executed trade record
public class Trade {
    private String id;                                    // private = unique trade identifier
    private Stock stock;
    private Trader buyer;                                 // private = buy-side participant
    private Trader seller;                                // private = sell-side participant
    private double price;                                 // private = execution price
    private int quantity;                                 // private = number of shares traded

    public Trade(String id, Stock stock, Trader buyer, Trader seller, double price, int quantity) {
        this.id = id; this.stock = stock; this.buyer = buyer; this.seller = seller;
        this.price = price; this.quantity = quantity;
    }
    public String getId() { return id; }
    public Trader getBuyer() { return buyer; }
    public Trader getSeller() { return seller; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    @Override public String toString() { return String.format("Trade: %s buys %d %s @ $%.2f from %s", buyer.getName(), quantity, stock, price, seller.getName()); }
}
