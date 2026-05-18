/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates payment gateway with parallel validation and atomic idempotency
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Payment Gateway Demo (Optimized) ===");
        System.out.println("Optimizations: Parallel validation, ConcurrentHashMap idempotency\n");

        PaymentGateway gateway = new PaymentGateway();
        gateway.registerProcessor("STRIPE", new PaymentProcessor.StripeProcessor());
        gateway.registerProcessor("PAYPAL", new PaymentProcessor.PayPalProcessor());

        System.out.println("--- Processing Payments (validators run in parallel) ---");
        gateway.processPayment(new PaymentRequest("key-001", 99.99, "USD", "4111111111111111", "STRIPE", "Purchase #1"));
        gateway.processPayment(new PaymentRequest("key-002", 250.00, "USD", "4222222222222222", "PAYPAL", "Subscription"));

        System.out.println("\n--- Atomic Idempotency (ConcurrentHashMap.putIfAbsent) ---");
        gateway.processPayment(new PaymentRequest("key-001", 99.99, "USD", "4111111111111111", "STRIPE", "Purchase #1"));

        System.out.println("\n--- All Transactions ---");
        for (Transaction t : gateway.getAllTransactions()) System.out.println("  " + t);

        System.out.println("\n=== Demo Complete ===");
    }
}
