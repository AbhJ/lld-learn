/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Order.java — Order with AtomicReference state machine

import java.util.concurrent.atomic.AtomicReference;

public class Order {
    private final String orderId;                   // final = ID never changes after construction; safe publication
    private final AtomicReference<OrderState> state; // AtomicReference = lock-free CAS state transitions
    private volatile String transitionBy;           // volatile = all threads see who won the race immediately

    public Order(String orderId) {
        this.orderId = orderId;
        this.state = new AtomicReference<>(OrderState.CREATED);
    }

    /**
     * CAS transition: only succeeds if current state matches expected.
     * cancel only from CREATED, pay only from CREATED.
     */
    public boolean tryTransition(OrderState expected, OrderState next, String actor) { // CAS = only one thread wins the transition
        if (state.compareAndSet(expected, next)) { // compareAndSet = atomic; fails if another thread already changed state
            transitionBy = actor;
            return true;
        }
        return false;
    }

    public OrderState getState() { return state.get(); }
    public String getOrderId() { return orderId; }
    public String getTransitionBy() { return transitionBy; }

    @Override
    public String toString() {
        return orderId + " [" + state.get() + "] by " + transitionBy;
    }
}
