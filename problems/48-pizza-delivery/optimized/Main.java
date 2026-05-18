/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the optimized pizza delivery
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Pizza Delivery Demo (Optimized - Decorator + ConcurrentHashMap + Topping Interning) ===");

        PizzaShop shop = new PizzaShop();

        System.out.println("\n--- Build Pizza (validated builder) ---");
        Pizza pizza1 = new PizzaBuilder().size(Size.LARGE).crust(Crust.THIN)
            .addToppings(Topping.MOZZARELLA, Topping.PEPPERONI, Topping.MUSHROOMS).build();
        List<PizzaDecorator> extras1 = new ArrayList<>();
        extras1.add(new ExtraCheese());
        extras1.add(new ExtraSauce());
        double total = pizza1.getBasePrice();
        for (PizzaDecorator d : extras1) total += d.getCost();
        System.out.println(pizza1.getName() + " total: $" + String.format("%.2f", total));

        System.out.println("\n--- Topping Interning (memory optimization) ---");
        // Build two more pizzas with the SAME topping selection — different size/crust, same toppings.
        // The interned topping-Set should be reference-equal across all three pizzas.
        Pizza pizza2 = new PizzaBuilder().size(Size.MEDIUM).crust(Crust.REGULAR)
            .addToppings(Topping.MOZZARELLA, Topping.PEPPERONI, Topping.MUSHROOMS).build();
        // Build with toppings in a different order — interning normalizes via EnumSet, so still shared.
        Pizza pizza3 = new PizzaBuilder().size(Size.SMALL).crust(Crust.STUFFED)
            .addToppings(Topping.MUSHROOMS, Topping.PEPPERONI, Topping.MOZZARELLA).build();
        Set<Topping> t1 = pizza1.getToppings();
        Set<Topping> t2 = pizza2.getToppings();
        Set<Topping> t3 = pizza3.getToppings();
        System.out.println("pizza1 toppings identityHashCode: " + System.identityHashCode(t1));
        System.out.println("pizza2 toppings identityHashCode: " + System.identityHashCode(t2));
        System.out.println("pizza3 toppings identityHashCode: " + System.identityHashCode(t3));
        System.out.println("pizza1.toppings == pizza2.toppings ? " + (t1 == t2));
        System.out.println("pizza2.toppings == pizza3.toppings ? " + (t2 == t3));
        // Build one with a DIFFERENT topping set — should NOT share with the above.
        Pizza pizza4 = new PizzaBuilder().addToppings(Topping.BBQ_CHICKEN, Topping.BACON).build();
        System.out.println("pizza1.toppings == pizza4.toppings ? " + (t1 == pizza4.getToppings())
            + " (different toppings, different interned set)");
        System.out.println("Distinct interned topping-sets cached: " + PizzaBuilder.internedSetCount());

        System.out.println("\n--- Create Order (running total) ---");
        Order order = shop.createOrder();
        order.addPizza(pizza1, extras1);
        System.out.println("Order total (O(1)): $" + String.format("%.2f", order.getTotal()));

        System.out.println("\n--- Process Order (concurrent tracking) ---");
        shop.processOrder(order);
        System.out.println("Order complete, removed from active tracking");

        System.out.println("\n=== Pizza Delivery Demo Complete ===");
    }
}
