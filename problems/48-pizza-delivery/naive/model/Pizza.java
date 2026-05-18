/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Pizza.java — Represents a configured pizza
import java.util.ArrayList;
import java.util.List;

public class Pizza {
    private Size size;              // private = pizza size set once at creation
    private Crust crust;            // private = crust type set once at creation
    private List<Topping> toppings; // private = toppings list encapsulated

    public Pizza(Size size, Crust crust, List<Topping> toppings) {
        this.size = size; this.crust = crust; this.toppings = new ArrayList<>(toppings);
    }

    public Size getSize() { return size; }
    public Crust getCrust() { return crust; }
    public List<Topping> getToppings() { return toppings; }

    // Fixed price calculation with sequential additions
    public double getBasePrice() {
        double price = size.getBasePrice() + crust.getExtraCost();
        for (Topping t : toppings) price += t.getPrice();
        return price;
    }

    public String getName() { return size.getDisplayName() + " " + crust.getDisplayName(); }
    @Override public String toString() { return getName(); }
}
