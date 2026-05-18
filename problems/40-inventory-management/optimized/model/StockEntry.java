/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/StockEntry.java — Quantity of a product at a specific warehouse
public class StockEntry {
    private int quantity;                  // private = only this class modifies stock level

    public StockEntry(int quantity) { this.quantity = quantity; }
    public void add(int qty) { this.quantity += qty; }
    public boolean remove(int qty) {
        if (quantity < qty) return false;
        quantity -= qty;
        return true;
    }
    public int getQuantity() { return quantity; }
}
