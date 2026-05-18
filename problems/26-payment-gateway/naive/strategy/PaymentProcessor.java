/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/PaymentProcessor.java — Pluggable payment processing algorithms
public interface PaymentProcessor {            // interface = contract that all processors must fulfill
    boolean processPayment(PaymentRequest request);
    boolean processRefund(String transactionId, double amount);
    String getName();

    class StripeProcessor implements PaymentProcessor { // implements = fulfills the PaymentProcessor contract
        private boolean simulateFailure = false;
        public void setSimulateFailure(boolean fail) { this.simulateFailure = fail; }
        @Override public boolean processPayment(PaymentRequest request) {
            System.out.println("  Stripe: Processing $" + String.format("%.2f", request.getAmount()));
            if (simulateFailure) { System.out.println("  Stripe: DECLINED"); return false; }
            System.out.println("  Stripe: APPROVED"); return true;
        }
        @Override public boolean processRefund(String txnId, double amount) { return true; }
        @Override public String getName() { return "STRIPE"; }
    }

    class PayPalProcessor implements PaymentProcessor { // implements = another processor fulfilling same interface
        @Override public boolean processPayment(PaymentRequest request) {
            System.out.println("  PayPal: Processing $" + String.format("%.2f", request.getAmount()));
            System.out.println("  PayPal: APPROVED"); return true;
        }
        @Override public boolean processRefund(String txnId, double amount) { return true; }
        @Override public String getName() { return "PAYPAL"; }
    }
}
