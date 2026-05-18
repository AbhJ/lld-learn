/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/LoggingObserver.java — Console logger; demonstrates observing vending events

class LoggingObserver implements VendingMachineObserver {
    @Override
    public void onProductDispensed(Product product, int changeReturnedCents) {
        System.out.println("  [event] sold: " + product.getName()
                + " (change=" + Coin.formatCents(changeReturnedCents) + ")");
    }

    @Override
    public void onLowStock(Product product, int remainingQuantity) {
        System.out.println("  [event] LOW STOCK: " + product.getName()
                + " has " + remainingQuantity + " left — restock soon");
    }

    @Override
    public void onSoldOut(Product product) {
        System.out.println("  [event] SOLD OUT: " + product.getName());
    }
}
