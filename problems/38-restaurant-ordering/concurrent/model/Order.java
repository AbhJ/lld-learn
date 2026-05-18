/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Order.java — Thread-safe order with lock for item addition and atomic state

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class Order {
    private final int id;                                      // final = safe publication; ID visible to all threads
    private final List<String> items = new ArrayList<>();       // guarded by itemsLock
    private final ReentrantLock itemsLock = new ReentrantLock(); // ReentrantLock = mutual exclusion for item list
    private final AtomicReference<OrderState> state = new AtomicReference<>(OrderState.BUILDING); // AtomicReference = CAS-based state transitions

    public Order(int id) {
        this.id = id;
    }

    public int getId() { return id; }

    /**
     * Add an item. Only allowed while in BUILDING state.
     * Waiter holds lock while building the order.
     */
    public boolean addItem(String item) {
        itemsLock.lock();
        try {
            if (state.get() != OrderState.BUILDING) return false;
            items.add(item);
            return true;
        } finally {
            itemsLock.unlock();
        }
    }

    /**
     * Submit the order. Transitions from BUILDING to SUBMITTED atomically.
     * Lock ensures no items are being added during transition.
     */
    public boolean submit() {
        itemsLock.lock();
        try {
            return state.compareAndSet(OrderState.BUILDING, OrderState.SUBMITTED); // CAS = only transitions if still BUILDING
        } finally {
            itemsLock.unlock();
        }
    }

    /**
     * Kitchen picks up the order. Only from SUBMITTED state.
     */
    public boolean startPreparing() {
        return state.compareAndSet(OrderState.SUBMITTED, OrderState.PREPARING); // CAS = prevents double-pickup
    }

    /**
     * Get a snapshot of items. Only valid after submission.
     */
    public List<String> getItems() {
        itemsLock.lock();
        try {
            return new ArrayList<>(items);
        } finally {
            itemsLock.unlock();
        }
    }

    public int getItemCount() {
        itemsLock.lock();
        try {
            return items.size();
        } finally {
            itemsLock.unlock();
        }
    }

    public OrderState getState() { return state.get(); }

    public boolean markReady() {
        return state.compareAndSet(OrderState.PREPARING, OrderState.READY); // CAS = atomic state advance
    }
}
