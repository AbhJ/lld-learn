/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the pizza delivery system
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Pizza Delivery Demo (Naive) ===");

        PizzaShop shop = new PizzaShop();

        System.out.println("\n--- Build Pizza ---");
        Pizza pizza1 = new PizzaBuilder().size(Size.LARGE).crust(Crust.THIN)
            .addToppings(Topping.MOZZARELLA, Topping.PEPPERONI, Topping.MUSHROOMS).build();
        List<PizzaDecorator> extras1 = new ArrayList<>();
        extras1.add(new ExtraCheese());
        System.out.println(pizza1.getName() + " + extras: $" + String.format("%.2f",
            pizza1.getBasePrice() + extras1.stream().mapToDouble(PizzaDecorator::getCost).sum()));

        System.out.println("\n--- Create Order ---");
        Order order = shop.createOrder();
        order.addPizza(pizza1, extras1);
        System.out.println("Order total: $" + String.format("%.2f", order.getTotal()));

        System.out.println("\n--- Process Order ---");
        shop.processOrder(order);

        System.out.println("\n=== Pizza Delivery Demo Complete ===");
    }
}
