/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/VendingMachineObserver.java — Observer contract for vending events
//
// Subscribers receive notifications when business events happen inside the
// machine. Examples: a console logger, an analytics sink, a restock alert
// service. The machine fires events; observers decide what to do with them.

interface VendingMachineObserver {
    /** A product was successfully dispensed. */
    void onProductDispensed(Product product, int changeReturnedCents);

    /** A product's stock has fallen at or below the configured low-stock threshold. */
    void onLowStock(Product product, int remainingQuantity);

    /** A product just hit zero stock. */
    void onSoldOut(Product product);
}
