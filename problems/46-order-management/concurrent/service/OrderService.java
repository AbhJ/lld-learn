/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/OrderService.java — CAS-based order state transitions

import java.util.concurrent.atomic.AtomicInteger;

public class OrderService {
    private final AtomicInteger paySuccessCount = new AtomicInteger(0);    // AtomicInteger = thread-safe counter without locking
    private final AtomicInteger cancelSuccessCount = new AtomicInteger(0); // AtomicInteger = many threads increment safely
    private final AtomicInteger payFailCount = new AtomicInteger(0);       // AtomicInteger = tracks failed pay attempts across threads
    private final AtomicInteger cancelFailCount = new AtomicInteger(0);    // AtomicInteger = tracks failed cancel attempts across threads

    /**
     * Attempt to pay for an order. Only succeeds from CREATED state.
     */
    public boolean pay(Order order, String actor) {
        boolean success = order.tryTransition(OrderState.CREATED, OrderState.PAID, actor);
        if (success) paySuccessCount.incrementAndGet();
        else payFailCount.incrementAndGet();
        return success;
    }

    /**
     * Attempt to cancel an order. Only succeeds from CREATED state.
     */
    public boolean cancel(Order order, String actor) {
        boolean success = order.tryTransition(OrderState.CREATED, OrderState.CANCELLED, actor);
        if (success) cancelSuccessCount.incrementAndGet();
        else cancelFailCount.incrementAndGet();
        return success;
    }

    public int getPaySuccessCount() { return paySuccessCount.get(); }
    public int getCancelSuccessCount() { return cancelSuccessCount.get(); }
    public int getPayFailCount() { return payFailCount.get(); }
    public int getCancelFailCount() { return cancelFailCount.get(); }
}
