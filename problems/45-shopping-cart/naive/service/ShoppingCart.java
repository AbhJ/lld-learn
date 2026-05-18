/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ShoppingCart.java — Manages cart items and recalculates total every time
import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {
    private List<CartItem> items;             // private = cart contents managed through methods
    private DiscountStrategy discountStrategy; // private = strategy pattern for discounts
    private Coupon appliedCoupon;             // private = single coupon per cart
    private List<CartDecorator> addOns;       // private = extra services (gift wrap, shipping)
    private TaxCalculator taxCalculator;      // private = tax logic encapsulated
    private String taxRegion;                 // private = determines tax rate
    private List<PriceObserver> priceObservers; // private = registered price-change listeners

    public ShoppingCart(String taxRegion) {
        this.items = new ArrayList<>(); this.addOns = new ArrayList<>();
        this.taxCalculator = new TaxCalculator(); this.taxRegion = taxRegion;
        this.priceObservers = new ArrayList<>();
    }

    public void addObserver(PriceObserver observer) { priceObservers.add(observer); }

    public void addItem(Product product, int quantity) {
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.incrementQuantity(quantity);
                System.out.println("Added: " + product.getName() + " x" + quantity);
                return;
            }
        }
        items.add(new CartItem(product, quantity));
        System.out.println("Added: " + product.getName() + " x" + quantity);
    }

    public void removeItem(String productId) { items.removeIf(item -> item.getProduct().getId().equals(productId)); }

    // Recalculates total from scratch every time
    public double getSubtotal() {
        return items.stream().mapToDouble(CartItem::getSubtotal).sum();
    }

    public void applyDiscount(DiscountStrategy strategy) {
        this.discountStrategy = strategy;
        double discount = strategy.calculateDiscount(items);
        System.out.println("Applied " + strategy.getDescription() + ": -$" + String.format("%.2f", discount));
    }

    public boolean applyCoupon(Coupon coupon) {
        if (coupon.isValid(getSubtotal())) {
            this.appliedCoupon = coupon;
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
        for (PriceObserver obs : priceObservers) obs.onPriceChanged(product, oldPrice, newPrice);
    }

    public void checkout() {
        double subtotal = getSubtotal();
        double discount = discountStrategy != null ? discountStrategy.calculateDiscount(items) : 0;
        double couponDiscount = appliedCoupon != null ? appliedCoupon.getDiscountStrategy().calculateDiscount(items) : 0;
        double taxable = subtotal - discount - couponDiscount;
        double tax = taxCalculator.calculateTax(taxable, taxRegion);
        double addOnsTotal = addOns.stream().mapToDouble(CartDecorator::getCost).sum();
        double total = taxable + tax + addOnsTotal;

        System.out.println("Checkout: Subtotal=$" + String.format("%.2f", subtotal) +
            " Discount=-$" + String.format("%.2f", discount + couponDiscount) +
            " Tax=$" + String.format("%.2f", tax) + " Total=$" + String.format("%.2f", total));
    }

    public List<CartItem> getItems() { return items; }
}
