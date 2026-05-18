/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/RealPaymentProcessor.java — Real subject: simulates the actual gateway call

public class RealPaymentProcessor implements PaymentProcessor {

    /** Performs the "real" charge — same logic Payment.process() used to inline. */
    @Override
    public boolean process(double amount, String userId) {
        if (amount <= 0) {
            return false; // gateway rejects non-positive amounts
        }
        // In a real system this would call out to Stripe/Adyen/etc.
        System.out.printf("  Payment processed: $%.2f for %s%n", amount, userId);
        return true;
    }
}
