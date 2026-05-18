/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/PaymentProxy.java — Proxy adding pre-auth, audit logging, and a single retry

public class PaymentProxy implements PaymentProcessor {
    private final RealPaymentProcessor inner; // the wrapped real subject

    public PaymentProxy(RealPaymentProcessor inner) {
        this.inner = inner;
    }

    @Override
    public boolean process(double amount, String userId) {
        System.out.printf("  [proxy] auth check for $%.2f%n", amount);
        boolean ok = inner.process(amount, userId);
        if (!ok) {
            ok = inner.process(amount, userId); // single retry
        }
        String status = ok ? "OK" : "FAIL";
        System.out.printf("  [proxy] audit: amount=%.2f user=%s status=%s%n", amount, userId, status);
        return ok;
    }
}
