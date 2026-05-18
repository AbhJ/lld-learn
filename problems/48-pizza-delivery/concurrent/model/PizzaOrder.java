/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/PizzaOrder.java — Pizza order with AtomicReference lifecycle

import java.util.concurrent.atomic.AtomicReference;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class PizzaOrder {
    private final String orderId;                       // final = immutable ID; safe publication to threads
    private final String address;                       // final = delivery address never changes
    private final AtomicReference<DeliveryState> state; // AtomicReference = CAS-based state transitions
    private final List<String> stateHistory;            // synchronizedList = thread-safe append of state log

    public PizzaOrder(String orderId, String address) {
        this.orderId = orderId;
        this.address = address;
        this.state = new AtomicReference<>(DeliveryState.PLACED);
        this.stateHistory = Collections.synchronizedList(new ArrayList<>());
        this.stateHistory.add(DeliveryState.PLACED.name());
    }

    /**
     * Advance state to the next expected state. CAS ensures only valid transitions.
     */
    public boolean advanceTo(DeliveryState expected, DeliveryState next) { // CAS = only valid sequential transitions succeed
        if (state.compareAndSet(expected, next)) { // compareAndSet = atomic; fails if another thread already advanced
            stateHistory.add(next.name());
            return true;
        }
        return false;
    }

    public DeliveryState getState() { return state.get(); }
    public String getOrderId() { return orderId; }
    public String getAddress() { return address; }
    public List<String> getStateHistory() { return new ArrayList<>(stateHistory); }

    public boolean isDelivered() { return state.get() == DeliveryState.DELIVERED; }

    @Override
    public String toString() {
        return orderId + " @ " + address + " [" + state.get() + "]";
    }
}
