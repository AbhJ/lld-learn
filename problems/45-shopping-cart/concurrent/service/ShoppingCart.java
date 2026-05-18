/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/ShoppingCart.java — ReentrantReadWriteLock for cart (reads during browse, write-lock during checkout)

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.atomic.AtomicInteger;

public class ShoppingCart {
    private final CopyOnWriteArrayList<CartItem> items; // CopyOnWriteArrayList = safe iteration while adds happen
    private final ReentrantReadWriteLock rwLock; // RWLock = many concurrent adds, exclusive checkout
    private final AtomicInteger addCount;     // AtomicInteger = lock-free counter for total adds
    private volatile boolean checkedOut;      // volatile = all threads instantly see checkout flag change

    public ShoppingCart() {
        this.items = new CopyOnWriteArrayList<>();
        this.rwLock = new ReentrantReadWriteLock();
        this.addCount = new AtomicInteger(0);
        this.checkedOut = false;
    }

    /**
     * Add item to cart. Uses read lock (multiple adds can happen concurrently).
     * Fails if checkout is in progress.
     */
    public boolean addItem(CartItem item) {
        rwLock.readLock().lock();
        try {
            if (checkedOut) return false;
            items.add(item);
            addCount.incrementAndGet();
            return true;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Checkout: acquires write lock to get a consistent snapshot.
     * No adds can happen during checkout.
     * Returns the snapshot of items at checkout time.
     */
    public List<CartItem> checkout() {
        rwLock.writeLock().lock();
        try {
            checkedOut = true;
            // Consistent snapshot — no concurrent modifications possible
            return new ArrayList<>(items);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Browse cart (safe iteration via CopyOnWriteArrayList).
     */
    public List<CartItem> browse() {
        return new ArrayList<>(items);
    }

    public int getItemCount() { return items.size(); }
    public int getAddCount() { return addCount.get(); }
    public boolean isCheckedOut() { return checkedOut; }

    public double getTotal() {
        double total = 0;
        for (CartItem item : items) {
            total += item.getTotal();
        }
        return total;
    }
}
