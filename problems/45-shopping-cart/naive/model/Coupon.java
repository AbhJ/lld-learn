/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Coupon.java — Represents a discount coupon with code and rules
public class Coupon {
    private String code;                      // private = coupon code encapsulated
    private DiscountStrategy discountStrategy; // private = strategy pattern for discount calculation
    private boolean used;                     // private = one-time-use enforced internally
    private double minimumOrder;              // private = minimum order threshold for validity

    public Coupon(String code, DiscountStrategy discountStrategy, double minimumOrder) {
        this.code = code; this.discountStrategy = discountStrategy; this.used = false; this.minimumOrder = minimumOrder;
    }

    public String getCode() { return code; }
    public DiscountStrategy getDiscountStrategy() { return discountStrategy; }
    public boolean isUsed() { return used; }
    public double getMinimumOrder() { return minimumOrder; }
    public boolean isValid(double orderTotal) { return !used && orderTotal >= minimumOrder; }
    public void markUsed() { this.used = true; }

    @Override public String toString() { return code + " (" + discountStrategy.getDescription() + ")"; }
}
