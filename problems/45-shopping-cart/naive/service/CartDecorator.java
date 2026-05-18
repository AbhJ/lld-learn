/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/CartDecorator.java — Adds extras (gift wrap, shipping) to cart
public abstract class CartDecorator {         // abstract = can't create generic decorator; use GiftWrap etc.
    protected String name;                    // protected = subclasses can access service name
    protected double cost;                    // protected = subclasses set their own cost

    public CartDecorator(String name, double cost) { this.name = name; this.cost = cost; }
    public String getName() { return name; }
    public double getCost() { return cost; }
    @Override public String toString() { return name + ": $" + String.format("%.2f", cost); }
}

class GiftWrap extends CartDecorator { public GiftWrap() { super("Gift Wrap", 5.99); } } // extends = concrete add-on
class ExpressShipping extends CartDecorator { public ExpressShipping() { super("Express Shipping", 14.99); } } // extends = concrete add-on
