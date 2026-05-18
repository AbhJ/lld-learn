/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Topping.java — Enumerates available pizza toppings
public enum Topping { // enum = fixed set of toppings with prices; type-safe
    MOZZARELLA(1.00), PEPPERONI(1.50), MUSHROOMS(1.00), OLIVES(0.75),
    ONIONS(0.75), PEPPERS(0.75), SAUSAGE(1.50), BBQ_CHICKEN(2.00), BACON(1.75);
    private double price; // private = topping price encapsulated per constant
    Topping(double price) { this.price = price; }
    public double getPrice() { return price; }
}
