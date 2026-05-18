/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating optimized food delivery with spatial grid dispatch

import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Food Delivery System (Optimized) ===\n");

        FoodDeliverySystem system = new FoodDeliverySystem();

        Restaurant pizzaPlace = new Restaurant("R-1", "Pizza Palace", "100 Food St", 40.7128, -74.0060);
        pizzaPlace.addMenuItem(new MenuItem("M-1", "Margherita Pizza", "Classic margherita", 12.99));
        pizzaPlace.addMenuItem(new MenuItem("M-2", "Pepperoni Pizza", "Loaded pepperoni", 14.99));
        pizzaPlace.addMenuItem(new MenuItem("M-3", "Garlic Bread", "Crispy garlic bread", 5.99));

        Restaurant sushiBar = new Restaurant("R-2", "Sushi Express", "200 Ocean Ave", 40.7200, -74.0100);
        sushiBar.addMenuItem(new MenuItem("M-5", "Salmon Roll", "8 pcs", 16.99));
        sushiBar.addMenuItem(new MenuItem("M-6", "Miso Soup", "Traditional", 4.99));

        system.registerRestaurant(pizzaPlace);
        system.registerRestaurant(sushiBar);

        DeliveryAgent agent1 = new DeliveryAgent("A-1", "Mike", 40.7130, -74.0065);
        DeliveryAgent agent2 = new DeliveryAgent("A-2", "Sarah", 40.7150, -74.0080);
        DeliveryAgent agent3 = new DeliveryAgent("A-3", "Tom", 40.7180, -74.0090);
        system.registerAgent(agent1);
        system.registerAgent(agent2);
        system.registerAgent(agent3);

        Customer alice = new Customer("C-1", "Alice", "alice@email.com", "555-0101",
                "50 Park Ave", 40.7500, -73.9800);

        // --- Test 1: Spatial Grid Dispatch via Command pattern ---
        System.out.println("--- Test 1: Order with Spatial Grid Strategy ---");
        Map<MenuItem, Integer> orderItems = new LinkedHashMap<>();
        orderItems.put(pizzaPlace.getMenu().get(0), 2);
        orderItems.put(pizzaPlace.getMenu().get(2), 1);

        // Command pattern: encapsulate placeOrder as an undoable command.
        CommandInvoker invoker = new CommandInvoker();
        PlaceOrderCommand placeCmd = new PlaceOrderCommand(system, alice, pizzaPlace, orderItems);
        invoker.run(placeCmd);
        Order order1 = placeCmd.getPlacedOrder();
        system.confirmOrder(order1);
        system.startPreparing(order1);
        system.assignAndDispatch(order1);
        system.deliverOrder(order1);
        System.out.println();

        // --- Test 2: Priority Dispatch Strategy ---
        System.out.println("--- Test 2: Priority Dispatch Strategy ---");
        system.setDeliveryStrategy(new PriorityDispatchStrategy());

        Map<MenuItem, Integer> items2 = new LinkedHashMap<>();
        items2.put(sushiBar.getMenu().get(0), 1);
        Order order2 = system.placeOrder(alice, sushiBar, items2);
        system.confirmOrder(order2);
        system.startPreparing(order2);
        system.assignAndDispatch(order2);
        system.deliverOrder(order2);
        System.out.println();

        // --- Test 3: Rating ---
        System.out.println("--- Test 3: Rate Order ---");
        Rating rating = system.rateOrder(order1, 4.5, 5.0, "Great food, fast delivery!");
        System.out.println(rating);
        System.out.println();

        // --- Test 4: Cancel via Command pattern ---
        System.out.println("--- Test 4: Cancel Order via Command ---");
        Map<MenuItem, Integer> items3 = new LinkedHashMap<>();
        items3.put(pizzaPlace.getMenu().get(1), 1);
        Order order3 = system.placeOrder(alice, pizzaPlace, items3);
        system.confirmOrder(order3);
        invoker.run(new CancelOrderCommand(system, order3));
        System.out.println("Order cancelled: " + order3.getState());
        System.out.println("Command history size: " + invoker.historySize());
        System.out.println();

        System.out.println("=== Food Delivery Demo Complete ===");
    }
}
