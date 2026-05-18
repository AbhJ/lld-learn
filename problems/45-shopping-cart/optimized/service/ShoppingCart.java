/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ShoppingCart.java — Cart with running total maintained on add/remove/update
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppingCart {
    private List<CartItem> items;
    private Map<String, CartItem> itemIndex;  // HashMap = O(1) item lookup by product ID
    private double runningSubtotal;           // running total = O(1) getSubtotal() vs O(n) recalculation
    private DiscountStrategy discountStrategy;
    private Coupon appliedCoupon;
    private List<CartDecorator> addOns;
    private TaxCalculator taxCalculator;
    private String taxRegion;
    private List<PriceObserver> priceObservers;
    private boolean discountDirty;             // dirty flag = recalculate discount only when cart changes
    private double cachedDiscount;            // cached value avoids re-running discount strategy

    public ShoppingCart(String taxRegion) {
        this.items = new ArrayList<>(); this.itemIndex = new HashMap<>();
        this.addOns = new ArrayList<>(); this.taxCalculator = new TaxCalculator();
        this.taxRegion = taxRegion; this.priceObservers = new ArrayList<>();
        this.runningSubtotal = 0; this.discountDirty = true;
    }

    public void addObserver(PriceObserver observer) { priceObservers.add(observer); }

    public void addItem(Product product, int quantity) {
        CartItem existing = itemIndex.get(product.getId());
        if (existing != null) {
            // WHY: Update running total incrementally — O(1) instead of O(n)
            runningSubtotal += product.getPrice() * quantity;
            existing.incrementQuantity(quantity);
        } else {
            CartItem newItem = new CartItem(product, quantity);
            items.add(newItem);
            itemIndex.put(product.getId(), newItem);
            runningSubtotal += product.getPrice() * quantity;
        }
        discountDirty = true;
        System.out.println("Added: " + product.getName() + " x" + quantity);
    }

    public void removeItem(String productId) {
        CartItem item = itemIndex.remove(productId);
        if (item != null) {
            // WHY: Subtract from running total — O(1) instead of recalculating
            runningSubtotal -= item.getSubtotal();
            items.remove(item);
            discountDirty = true;
        }
    }

    public void updateQuantity(String productId, int newQuantity) {
        CartItem item = itemIndex.get(productId);
        if (item != null) {
            double oldSubtotal = item.getSubtotal();
            item.setQuantity(newQuantity);
            // WHY: Adjust running total by delta — O(1)
            runningSubtotal += (item.getSubtotal() - oldSubtotal);
            discountDirty = true;
        }
    }

    // WHY: O(1) — returns pre-computed running total instead of iterating all items
    public double getSubtotal() { return runningSubtotal; }

    public void applyDiscount(DiscountStrategy strategy) {
        this.discountStrategy = strategy;
        this.discountDirty = true;
        double discount = strategy.calculateDiscount(items);
        System.out.println("Applied " + strategy.getDescription() + ": -$" + String.format("%.2f", discount));
    }

    public boolean applyCoupon(Coupon coupon) {
        if (coupon.isValid(runningSubtotal)) {
            this.appliedCoupon = coupon;
            this.discountDirty = true;
            double discount = coupon.getDiscountStrategy().calculateDiscount(items);
            System.out.println("Applied coupon " + coupon.getCode() + ": -$" + String.format("%.2f", discount));
            coupon.markUsed();
            return true;
        }
        System.out.println("Coupon " + coupon.getCode() + " is not valid");
        return false;
    }

    public void addService(CartDecorator service) {
        addOns.add(service);
        System.out.println(service.getName() + " added: +$" + String.format("%.2f", service.getCost()));
    }

    public void notifyPriceChange(Product product, double oldPrice, double newPrice) {
        // WHY: Event-driven price sync — update running total on price change
        CartItem item = itemIndex.get(product.getId());
        if (item != null) {
            double priceDelta = (newPrice - oldPrice) * item.getQuantity();
            runningSubtotal += priceDelta;
            discountDirty = true;
        }
        for (PriceObserver obs : priceObservers) obs.onPriceChanged(product, oldPrice, newPrice);
    }

    public void checkout() {
        double discount = discountStrategy != null ? discountStrategy.calculateDiscount(items) : 0;
        double couponDiscount = appliedCoupon != null ? appliedCoupon.getDiscountStrategy().calculateDiscount(items) : 0;
        double taxable = runningSubtotal - discount - couponDiscount;
        double tax = taxCalculator.calculateTax(taxable, taxRegion);
        double addOnsTotal = addOns.stream().mapToDouble(CartDecorator::getCost).sum();
        double total = taxable + tax + addOnsTotal;

        System.out.println("Checkout: Subtotal=$" + String.format("%.2f", runningSubtotal) +
            " Discount=-$" + String.format("%.2f", discount + couponDiscount) +
            " Tax=$" + String.format("%.2f", tax) + " Total=$" + String.format("%.2f", total));
    }

    public List<CartItem> getItems() { return items; }
}
