/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/BillingStrategy.java — Configurable bill calculation
// DESIGN PATTERN: Strategy
public interface BillingStrategy {      // interface = contract for different billing approaches
    double calculateTotal(double subtotal);
    String getDescription();
}

class DineInBilling implements BillingStrategy { // implements = fulfills BillingStrategy contract
    private final double servicePercent;         // final = service charge fixed at creation
    public DineInBilling(double pct) { this.servicePercent = pct; }
    @Override public double calculateTotal(double subtotal) { return subtotal * (1 + servicePercent / 100); }
    @Override public String getDescription() { return "Dine-In (" + (int)servicePercent + "% service)"; }
}

class TakeAwayBilling implements BillingStrategy { // implements = different billing strategy
    @Override public double calculateTotal(double subtotal) { return subtotal * 1.05; }
    @Override public String getDescription() { return "Takeaway (5% packaging)"; }
}
