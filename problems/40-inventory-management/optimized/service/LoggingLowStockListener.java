/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/LoggingLowStockListener.java — Console logger; demonstrates observing low-stock events

class LoggingLowStockListener implements LowStockListener {
    @Override
    public void onLowStock(String sku, String warehouseId, int remainingQuantity) {
        System.out.println("  [event] LOW STOCK: sku=" + sku
                + " at warehouse=" + warehouseId
                + " (qty=" + remainingQuantity + ") — restock soon");
    }
}
