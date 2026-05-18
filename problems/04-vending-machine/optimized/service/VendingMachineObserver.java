/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/VendingMachineObserver.java — Observer contract for vending events

interface VendingMachineObserver {
    void onProductDispensed(Product product, int changeReturnedCents);
    void onLowStock(Product product, int remainingQuantity);
    void onSoldOut(Product product);
}
