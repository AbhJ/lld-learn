/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/VendingMachine.java — Thread-safe vending with CAS on product quantity

package service;

import model.Product;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class VendingMachine {
    private final ConcurrentHashMap<String, Product> products; // ConcurrentHashMap = thread-safe product lookup from many buyers
    private final List<String> transactionLog; // synchronizedList = safe for concurrent log appends

    public VendingMachine() {
        this.products = new ConcurrentHashMap<>();
        this.transactionLog = Collections.synchronizedList(new ArrayList<>());
    }

    public void addProduct(Product product) {
        products.put(product.getName(), product);
    }

    /**
     * Attempt to purchase a product. Uses CAS (AtomicInteger.compareAndSet loop).
     * Returns true if purchase succeeded (stock was available).
     */
    public boolean purchase(String userId, String productName) {
        Product product = products.get(productName);
        if (product == null) {
            transactionLog.add(userId + " FAILED — product not found: " + productName);
            return false;
        }

        boolean success = product.tryPurchase();
        if (success) {
            transactionLog.add(userId + " purchased \"" + productName + "\"");
        } else {
            transactionLog.add(userId + " REJECTED — out of stock: " + productName);
        }
        return success;
    }

    public Product getProduct(String name) { return products.get(name); }
    public List<String> getTransactionLog() { return transactionLog; }
}
