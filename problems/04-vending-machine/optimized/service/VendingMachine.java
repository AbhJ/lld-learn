/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/VendingMachine.java — State context with atomic inserted-amount tracking,
//                                pluggable change strategy, and thread-safe observer fan-out.

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Optimized:
 *  - AtomicInteger for the inserted amount (lock-free coin insertion under contention).
 *  - CopyOnWriteArrayList for observers (lock-free iteration during fan-out).
 *  - Pluggable ChangeStrategy (real Strategy pattern, not a static utility).
 */
class VendingMachine {
    private State state;
    private final Inventory inventory;
    private final AtomicInteger insertedAmountInCents;
    private Product selectedProduct;

    private ChangeStrategy changeStrategy;
    private final List<VendingMachineObserver> observers = new CopyOnWriteArrayList<>();

    private static final int LOW_STOCK_THRESHOLD = 2;

    public VendingMachine() {
        this(new GreedyChangeStrategy());
    }

    public VendingMachine(ChangeStrategy changeStrategy) {
        this.state = new IdleState();
        this.inventory = new Inventory();
        this.insertedAmountInCents = new AtomicInteger(0);
        this.selectedProduct = null;
        this.changeStrategy = changeStrategy;
    }

    public void insertCoin(Coin coin) { state.insertCoin(this, coin); }
    public void selectProduct(String productCode) { state.selectProduct(this, productCode); }
    public void cancel() { state.cancel(this); }

    void setState(State state) { this.state = state; }
    State getState() { return state; }

    void addInsertedAmount(int cents) { this.insertedAmountInCents.addAndGet(cents); }
    void deductInsertedAmount(int cents) { this.insertedAmountInCents.addAndGet(-cents); }
    void resetInsertedAmount() { this.insertedAmountInCents.set(0); }
    int getInsertedAmount() { return insertedAmountInCents.get(); }

    void setSelectedProduct(Product product) { this.selectedProduct = product; }
    Product getSelectedProduct() { return selectedProduct; }
    Inventory getInventory() { return inventory; }

    ChangeStrategy getChangeStrategy() { return changeStrategy; }
    public void setChangeStrategy(ChangeStrategy strategy) { this.changeStrategy = strategy; }

    public void addObserver(VendingMachineObserver observer) { observers.add(observer); }
    public void removeObserver(VendingMachineObserver observer) { observers.remove(observer); }

    void fireDispensed(Product product, int changeReturnedCents) {
        for (VendingMachineObserver o : observers) {
            o.onProductDispensed(product, changeReturnedCents);
        }
        int remaining = inventory.getQuantity(product.getCode());
        if (remaining == 0) {
            for (VendingMachineObserver o : observers) o.onSoldOut(product);
        } else if (remaining <= LOW_STOCK_THRESHOLD) {
            for (VendingMachineObserver o : observers) o.onLowStock(product, remaining);
        }
    }

    public String getStatus() {
        return "State: " + state.getName() + " | Inserted: " + Coin.formatCents(insertedAmountInCents.get());
    }
}
