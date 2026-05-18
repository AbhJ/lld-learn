/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Inventory.java — Product stock management with add/remove/check operations

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

class Inventory {
    private Map<String, Product> products;  // private = internal product catalog
    private Map<String, Integer> quantities; // private = stock levels managed internally

    public Inventory() {
        this.products = new HashMap<>();
        this.quantities = new HashMap<>();
    }

    public void addProduct(Product product, int quantity) {
        products.put(product.getCode(), product);
        quantities.put(product.getCode(), quantities.getOrDefault(product.getCode(), 0) + quantity);
    }

    public Product getProduct(String code) {
        return products.get(code);
    }

    public boolean isAvailable(String code) {
        return quantities.getOrDefault(code, 0) > 0;
    }

    public boolean dispense(String code) {
        if (!isAvailable(code)) return false;
        quantities.put(code, quantities.get(code) - 1);
        return true;
    }

    public int getQuantity(String code) {
        return quantities.getOrDefault(code, 0);
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    public String getDisplayInfo() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Product> entry : products.entrySet()) {
            Product p = entry.getValue();
            int qty = quantities.get(entry.getKey());
            sb.append("  ").append(p).append(" - Stock: ").append(qty);
            if (qty == 0) sb.append(" [SOLD OUT]");
            sb.append("\n");
        }
        return sb.toString().trim();
    }
}
