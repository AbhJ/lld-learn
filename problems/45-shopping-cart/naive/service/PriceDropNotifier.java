/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/PriceDropNotifier.java — Concrete observer that notifies users of price drops
//
// This class IMPLEMENTS PriceObserver (defined in PriceObserver.java).

class PriceDropNotifier implements PriceObserver { // implements = fulfills observer contract
    private String userName;          // private = who gets notified

    public PriceDropNotifier(String userName) { this.userName = userName; }

    @Override
    public void onPriceChanged(Product product, double oldPrice, double newPrice) {
        if (newPrice < oldPrice) {
            System.out.println("[" + userName + "] Price drop: " + product.getName() +
                " $" + String.format("%.2f", oldPrice) + " -> $" + String.format("%.2f", newPrice));
        }
    }
}
