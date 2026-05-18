/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Pizza.java — Pizza with composable decorator pricing + interned topping set
import java.util.Set;

public class Pizza {
    private Size size;              // private = pizza size set once at creation
    private Crust crust;            // private = crust type set once at creation
    // Interned, immutable topping set: Pizzas with identical toppings share the SAME Set instance.
    // The interning is performed by PizzaBuilder; here we just hold the reference.
    private Set<Topping> toppings;
    // Pre-computed base price avoids recalculating on every access
    private double cachedBasePrice; // cached = O(1) getBasePrice() instead of O(toppings) each time

    public Pizza(Size size, Crust crust, Set<Topping> toppings) {
        this.size = size; this.crust = crust;
        // Reference-share the interned set — do NOT copy, that would defeat the interning.
        this.toppings = toppings;
        this.cachedBasePrice = computeBasePrice();
    }

    private double computeBasePrice() {
        double price = size.getBasePrice() + crust.getExtraCost();
        for (Topping t : toppings) price += t.getPrice();
        return price;
    }

    public Size getSize() { return size; }
    public Crust getCrust() { return crust; }
    public Set<Topping> getToppings() { return toppings; }
    public double getBasePrice() { return cachedBasePrice; }
    public String getName() { return size.getDisplayName() + " " + crust.getDisplayName(); }
    @Override public String toString() { return getName(); }
}
