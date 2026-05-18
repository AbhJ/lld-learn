/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/LowStockListener.java — Observer contract for low-stock events
//
// Subscribers receive notifications when a product's stock at a warehouse
// drops at or below its reorder point. Examples: a console logger, an email
// dispatcher, a procurement service. The system fires events; listeners act.

interface LowStockListener {
    /** A product's stock fell to or below its reorder point at a warehouse. */
    void onLowStock(String sku, String warehouseId, int remainingQuantity);
}
