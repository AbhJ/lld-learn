/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Product.java — Vendable product with name, price, and selection code

class Product {
    private String code;              // private = only this class manages the code
    private String name;              // private = encapsulated; accessed via getter
    private int priceInCents;         // private = price hidden; read via getter

    public Product(String code, String name, int priceInCents) {
        this.code = code;
        this.name = name;
        this.priceInCents = priceInCents;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public int getPriceInCents() { return priceInCents; }

    @Override
    public String toString() {
        return code + ": " + name + " (" + Coin.formatCents(priceInCents) + ")";
    }
}
