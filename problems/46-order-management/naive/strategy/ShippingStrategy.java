/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/ShippingStrategy.java — Defines shipping cost and method calculation
// DESIGN PATTERN: Strategy
public interface ShippingStrategy { // interface = contract; shipping methods must provide cost/time
    double getCost();
    String getEstimatedDelivery();
    String getName();
}

class StandardShipping implements ShippingStrategy { // implements = provides concrete shipping costs
    @Override public double getCost() { return 5.99; }
    @Override public String getEstimatedDelivery() { return "5-7 business days"; }
    @Override public String getName() { return "Standard"; }
}

class ExpressShipping implements ShippingStrategy { // implements = faster shipping, higher cost
    @Override public double getCost() { return 14.99; }
    @Override public String getEstimatedDelivery() { return "2-3 business days"; }
    @Override public String getName() { return "Express"; }
}

class OvernightShipping implements ShippingStrategy { // implements = fastest/costliest option
    @Override public double getCost() { return 29.99; }
    @Override public String getEstimatedDelivery() { return "Next business day"; }
    @Override public String getName() { return "Overnight"; }
}
