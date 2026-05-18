/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/VendingMachine.java — State context with pluggable change strategy and observer fan-out

import java.util.ArrayList;
import java.util.List;

class VendingMachine {
    private State state;                    // current FSM state; changed via setState()
    private Inventory inventory;            // product stock managed internally
    private int insertedAmountInCents;      // total cents the user has inserted but not yet spent
    private Product selectedProduct;        // what the user chose; set during selection

    // Strategy: pluggable change-making algorithm.
    private ChangeStrategy changeStrategy;

    // Observer: fan-out for vending events (sales, low stock, sold out).
    private final List<VendingMachineObserver> observers = new ArrayList<>();

    /** Default ctor uses the greedy change strategy. */
    public VendingMachine() {
        this(new GreedyChangeStrategy());
    }

    /** Inject a different change strategy (e.g. for testing or alternative coin systems). */
    public VendingMachine(ChangeStrategy changeStrategy) {
        this.state = new IdleState();
        this.inventory = new Inventory();
        this.insertedAmountInCents = 0;
        this.selectedProduct = null;
        this.changeStrategy = changeStrategy;
    }

    public void insertCoin(Coin coin) {
        state.insertCoin(this, coin);
    }

    public void selectProduct(String productCode) {
        state.selectProduct(this, productCode);
    }

    public void cancel() {
        state.cancel(this);
    }

    void setState(State state) { this.state = state; }
    State getState() { return state; }

    void addInsertedAmount(int cents) { this.insertedAmountInCents += cents; }
    void deductInsertedAmount(int cents) { this.insertedAmountInCents -= cents; }
    void resetInsertedAmount() { this.insertedAmountInCents = 0; }
    int getInsertedAmount() { return insertedAmountInCents; }

    void setSelectedProduct(Product product) { this.selectedProduct = product; }
    Product getSelectedProduct() { return selectedProduct; }
    Inventory getInventory() { return inventory; }

    ChangeStrategy getChangeStrategy() { return changeStrategy; }
    public void setChangeStrategy(ChangeStrategy strategy) { this.changeStrategy = strategy; }

    // === Observer plumbing ===

    public void addObserver(VendingMachineObserver observer) { observers.add(observer); }
    public void removeObserver(VendingMachineObserver observer) { observers.remove(observer); }

    /** Threshold under which onLowStock fires after a successful dispense. */
    private static final int LOW_STOCK_THRESHOLD = 2;

    /** Called by DispensingState after a successful dispense. */
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
        return "State: " + state.getName() + " | Inserted: " + Coin.formatCents(insertedAmountInCents);
    }
}
