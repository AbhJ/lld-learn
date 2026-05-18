/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/PizzaBuilder.java — Fluent builder for constructing custom pizzas
import java.util.ArrayList;
import java.util.List;

public class PizzaBuilder { // Builder pattern: constructs a Pizza step-by-step
    private Size size = Size.MEDIUM;            // private = default size; overridden by size()
    private Crust crust = Crust.REGULAR;        // private = default crust; overridden by crust()
    private List<Topping> toppings = new ArrayList<>(); // private = accumulated toppings

    public PizzaBuilder size(Size size) { this.size = size; return this; }
    public PizzaBuilder crust(Crust crust) { this.crust = crust; return this; }
    public PizzaBuilder addTopping(Topping t) { toppings.add(t); return this; }
    public PizzaBuilder addToppings(Topping... ts) { for (Topping t : ts) toppings.add(t); return this; }
    public Pizza build() { return new Pizza(size, crust, toppings); }
}
