/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/BillingStrategy.java — Bill calculation strategies
// DESIGN PATTERN: Strategy
public interface BillingStrategy {      // interface = contract for billing calculations
    double calculateTotal(double subtotal);
    String getDescription();
}

class DineInBilling implements BillingStrategy { // implements = fulfills BillingStrategy
    private final double pct;                    // final = service percent is fixed
    public DineInBilling(double pct) { this.pct = pct; }
    @Override public double calculateTotal(double subtotal) { return subtotal * (1 + pct / 100); }
    @Override public String getDescription() { return "Dine-In (" + (int)pct + "% service)"; }
}
