/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/PaymentProcessor.java — Subject interface for the Proxy pattern over payments

public interface PaymentProcessor {
    /** Processes a payment for the given user; returns true on success. */
    boolean process(double amount, String userId);
}
