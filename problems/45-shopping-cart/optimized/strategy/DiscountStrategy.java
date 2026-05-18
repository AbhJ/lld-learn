/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/DiscountStrategy.java — Defines how discounts are calculated
// DESIGN PATTERN: Strategy
import java.util.List;

public interface DiscountStrategy {           // interface = strategy pattern for discount calculations
    double calculateDiscount(List<CartItem> items);
    String getDescription();
}

class PercentageDiscount implements DiscountStrategy {
    private double percentage;
    public PercentageDiscount(double percentage) { this.percentage = percentage; }

    @Override public double calculateDiscount(List<CartItem> items) {
        double total = items.stream().mapToDouble(CartItem::getSubtotal).sum();
        return total * (percentage / 100.0);
    }
    @Override public String getDescription() { return String.format("%.0f%%", percentage); }
}

class FlatDiscount implements DiscountStrategy {
    private double amount;
    public FlatDiscount(double amount) { this.amount = amount; }

    @Override public double calculateDiscount(List<CartItem> items) {
        double total = items.stream().mapToDouble(CartItem::getSubtotal).sum();
        return Math.min(amount, total);
    }
    @Override public String getDescription() { return String.format("$%.2f off", amount); }
}

class BuyOneGetOne implements DiscountStrategy {
    private String productId;
    public BuyOneGetOne(String productId) { this.productId = productId; }

    @Override public double calculateDiscount(List<CartItem> items) {
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId) && item.getQuantity() >= 2) {
                return (item.getQuantity() / 2) * item.getProduct().getPrice();
            }
        }
        return 0;
    }
    @Override public String getDescription() { return "Buy One Get One Free"; }
}
