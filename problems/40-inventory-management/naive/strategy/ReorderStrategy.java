/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/ReorderStrategy.java — Determines reorder quantities
// DESIGN PATTERN: Strategy
public interface ReorderStrategy {      // interface = contract for different reorder policies
    int calculateReorderQuantity(Product product, int currentStock);
    String getName();
}

class FixedQuantityReorder implements ReorderStrategy { // implements = one reorder policy
    private final int qty;                              // final = reorder quantity fixed at creation
    public FixedQuantityReorder(int qty) { this.qty = qty; }
    @Override public int calculateReorderQuantity(Product p, int current) { return qty; }
    @Override public String getName() { return "Fixed(" + qty + ")"; }
}
