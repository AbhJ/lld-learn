/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/PriceObserver.java — Observer pattern interface for price change events
// DESIGN PATTERN: Observer
//
// WHO IMPLEMENTS THIS? → PriceDropNotifier (in PriceDropNotifier.java)
// WHO CALLS IT? → Cart/ProductCatalog calls observers when product prices change
// WHY? → Decouples "price changed" from "notify user about the drop".

interface PriceObserver {             // interface = observer contract for price-change events
    void onPriceChanged(Product product, double oldPrice, double newPrice);
}
