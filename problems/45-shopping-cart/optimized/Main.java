/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the optimized shopping cart
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Shopping Cart Demo (Optimized - Running Total) ===");

        ShoppingCart cart = new ShoppingCart("CA");
        cart.addObserver(new PriceDropNotifier("Alice"));

        Product laptop = new Product("p1", "Laptop", 999.99, "Electronics");
        Product headphones = new Product("p2", "Headphones", 79.99, "Electronics");
        Product mouse = new Product("p3", "Mouse", 29.99, "Accessories");

        System.out.println("\n--- Adding Items (running total updated incrementally) ---");
        cart.addItem(laptop, 1);
        cart.addItem(headphones, 2);
        cart.addItem(mouse, 1);
        System.out.println("Subtotal (O(1)): $" + String.format("%.2f", cart.getSubtotal()));

        System.out.println("\n--- Discount ---");
        cart.applyDiscount(new PercentageDiscount(10));

        System.out.println("\n--- Coupon ---");
        cart.applyCoupon(new Coupon("FLAT50", new FlatDiscount(50.00), 100.0));

        System.out.println("\n--- Add-ons ---");
        cart.addService(new GiftWrap());
        cart.addService(new ExpressShipping());

        System.out.println("\n--- Checkout ---");
        cart.checkout();

        System.out.println("\n--- Price Drop (event-driven sync) ---");
        double oldPrice = laptop.getPrice();
        laptop.setPrice(899.99);
        cart.notifyPriceChange(laptop, oldPrice, 899.99);
        System.out.println("Subtotal after price drop: $" + String.format("%.2f", cart.getSubtotal()));

        // === Test: Composite — combine multiple discount strategies as one ===
        System.out.println("\n--- Composite Discount ---");
        DiscountStrategy combined = new CompositeDiscount(
                new PercentageDiscount(10),
                new FlatDiscount(50.00));
        System.out.println("  Description: " + combined.getDescription());
        System.out.printf("  Discount on cart: $%.2f%n", combined.calculateDiscount(cart.getItems()));

        System.out.println("\n=== Shopping Cart Demo Complete ===");
    }
}
