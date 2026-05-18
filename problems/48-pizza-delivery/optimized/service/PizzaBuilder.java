/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/PizzaBuilder.java — Builder with validation + topping-set interning
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PizzaBuilder { // Builder pattern: constructs a Pizza step-by-step with validation
    private Size size = Size.MEDIUM;            // private = default size; overridden by size()
    private Crust crust = Crust.REGULAR;        // private = default crust; overridden by crust()
    private List<Topping> toppings = new ArrayList<>(); // private = accumulated toppings
    private static final int MAX_TOPPINGS = 10; // static final = constant shared across all builders

    // Topping-set interning cache: maps a topping-set "key" to the canonical immutable Set instance.
    // Two pizzas built with the same toppings share the SAME Set object — saves heap at scale
    // (e.g. 1M pizzas with the same 3 toppings allocate the topping-set once, not 1M times).
    // Synchronized via the cache itself; HashMap is fine for a long-lived process-wide intern table.
    private static final Map<Set<Topping>, Set<Topping>> TOPPING_SET_CACHE = new HashMap<>();

    public PizzaBuilder size(Size size) { this.size = size; return this; }
    public PizzaBuilder crust(Crust crust) { this.crust = crust; return this; }
    public PizzaBuilder addTopping(Topping t) {
        // WHY: Validation at build time prevents invalid pizza configurations
        if (toppings.size() >= MAX_TOPPINGS) throw new IllegalStateException("Max " + MAX_TOPPINGS + " toppings");
        toppings.add(t); return this;
    }
    public PizzaBuilder addToppings(Topping... ts) { for (Topping t : ts) addTopping(t); return this; }
    public Pizza build() {
        if (toppings.isEmpty()) throw new IllegalStateException("At least one topping required");
        Set<Topping> interned = internToppingSet(toppings);
        return new Pizza(size, crust, interned);
    }

    // Returns the canonical immutable Set<Topping> for this multiset of toppings.
    // Identical topping selections (regardless of insertion order) collapse to one shared instance.
    private static synchronized Set<Topping> internToppingSet(List<Topping> source) {
        // EnumSet is the most compact representation for an enum-keyed set (bit vector under the hood).
        EnumSet<Topping> key = EnumSet.copyOf(source);
        Set<Topping> existing = TOPPING_SET_CACHE.get(key);
        if (existing != null) return existing;
        Set<Topping> canonical = Collections.unmodifiableSet(key);
        TOPPING_SET_CACHE.put(key, canonical);
        return canonical;
    }

    // Test-only hook: lets demos verify identity-equality of interned sets without exposing the cache.
    public static int internedSetCount() { return TOPPING_SET_CACHE.size(); }
}
