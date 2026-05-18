/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the food delivery system

/*
 * VARIATIONS FREQUENTLY ASKED:
 * 1. Multi-restaurant cart - Order from multiple restaurants, separate delivery
 * 2. Subscription (DashPass) - Free delivery, reduced fees, minimum order
 * 3. Live kitchen tracking - Real-time prep status, estimated times per item
 * 4. Group ordering - Shared cart, split payment, individual item selection
 * 5. Scheduled delivery - Future time slots, batch preparation optimization
 *
 * See VARIATIONS.md for full solution approaches.
 */
import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Food Delivery System Demo ===\n");

        FoodDeliverySystem system = new FoodDeliverySystem();

        // Setup restaurants
        Restaurant pizzaPlace = new Restaurant("R-1", "Pizza Palace", "100 Food St", 40.7128, -74.0060);
        pizzaPlace.addMenuItem(new MenuItem("M-1", "Margherita Pizza", "Classic margherita", 12.99));
        pizzaPlace.addMenuItem(new MenuItem("M-2", "Pepperoni Pizza", "Loaded pepperoni", 14.99));
        pizzaPlace.addMenuItem(new MenuItem("M-3", "Garlic Bread", "Crispy garlic bread", 5.99));
        pizzaPlace.addMenuItem(new MenuItem("M-4", "Cola", "330ml can", 2.50));

        Restaurant sushiBar = new Restaurant("R-2", "Sushi Express", "200 Ocean Ave", 40.7200, -74.0100);
        sushiBar.addMenuItem(new MenuItem("M-5", "Salmon Roll", "8 pcs", 16.99));
        sushiBar.addMenuItem(new MenuItem("M-6", "Tuna Sashimi", "6 pcs", 18.99));
        sushiBar.addMenuItem(new MenuItem("M-7", "Miso Soup", "Traditional", 4.99));

        system.registerRestaurant(pizzaPlace);
        system.registerRestaurant(sushiBar);

        // Setup delivery agents
        DeliveryAgent agent1 = new DeliveryAgent("A-1", "Mike", 40.7130, -74.0065);
        DeliveryAgent agent2 = new DeliveryAgent("A-2", "Sarah", 40.7150, -74.0080);
        DeliveryAgent agent3 = new DeliveryAgent("A-3", "Tom", 40.7180, -74.0090);
        system.registerAgent(agent1);
        system.registerAgent(agent2);
        system.registerAgent(agent3);

        // Setup customers
        Customer alice = new Customer("C-1", "Alice", "alice@email.com", "555-0101",
                "50 Park Ave", 40.7500, -73.9800);
        Customer bob = new Customer("C-2", "Bob", "bob@email.com", "555-0102",
                "75 Broadway", 40.7300, -73.9900);

        // --- Test 1: Browse Menu ---
        System.out.println("--- Test 1: Browse Available Restaurants ---");
        for (Restaurant r : system.getOpenRestaurants()) {
            System.out.println("  " + r.getName() + ":");
            for (MenuItem item : r.getAvailableMenu()) {
                System.out.println("    " + item);
            }
        }
        System.out.println();

        // --- Test 2: Place Order (Nearest Agent Strategy) via Command ---
        System.out.println("--- Test 2: Place Order (Nearest Agent Strategy) ---");
        Map<MenuItem, Integer> orderItems = new LinkedHashMap<>();
        orderItems.put(pizzaPlace.getMenu().get(0), 2); // 2x Margherita
        orderItems.put(pizzaPlace.getMenu().get(2), 1); // 1x Garlic Bread
        orderItems.put(pizzaPlace.getMenu().get(3), 2); // 2x Cola

        // Command pattern: encapsulate placeOrder as an undoable command.
        CommandInvoker invoker = new CommandInvoker();
        PlaceOrderCommand placeCmd = new PlaceOrderCommand(system, alice, pizzaPlace, orderItems);
        invoker.run(placeCmd);
        Order order1 = placeCmd.getPlacedOrder();
        System.out.println();

        // --- Test 3: Order Lifecycle ---
        System.out.println("--- Test 3: Full Order Lifecycle ---");
        system.confirmOrder(order1);
        system.startPreparing(order1);
        system.assignAndDispatch(order1);
        system.deliverOrder(order1);
        System.out.println();

        // --- Test 4: Rate Order ---
        System.out.println("--- Test 4: Rate Order ---");
        Rating rating = system.rateOrder(order1, 4.5, 5.0, "Great food, fast delivery!");
        System.out.println(rating);
        System.out.printf("Pizza Palace avg rating: %.1f%n", pizzaPlace.getAverageRating());
        System.out.printf("Agent Mike avg rating: %.1f%n", agent1.getAverageRating());
        System.out.println();

        // --- Test 5: Least Busy Strategy ---
        System.out.println("--- Test 5: Least Busy Strategy ---");
        system.setDeliveryStrategy(new LeastBusyStrategy());

        // Give Mike another order to make him busy
        Map<MenuItem, Integer> items2 = new LinkedHashMap<>();
        items2.put(sushiBar.getMenu().get(0), 1);
        items2.put(sushiBar.getMenu().get(2), 1);

        Order order2 = system.placeOrder(bob, sushiBar, items2);
        system.confirmOrder(order2);
        system.startPreparing(order2);
        system.assignAndDispatch(order2);
        System.out.println();

        // --- Test 6: Cancel Order via Command ---
        System.out.println("--- Test 6: Cancel Order ---");
        Map<MenuItem, Integer> items3 = new LinkedHashMap<>();
        items3.put(pizzaPlace.getMenu().get(1), 1);
        Order order3 = system.placeOrder(alice, pizzaPlace, items3);
        system.confirmOrder(order3);
        // Command pattern: encapsulate cancellation; invoker tracks history for potential undo.
        invoker.run(new CancelOrderCommand(system, order3));
        System.out.println("Order cancelled: " + order3.getState());
        System.out.println("Command history size: " + invoker.historySize());
        System.out.println();

        // --- Test 7: Invalid State Transition ---
        System.out.println("--- Test 7: Invalid State Transition ---");
        Map<MenuItem, Integer> items4 = new LinkedHashMap<>();
        items4.put(pizzaPlace.getMenu().get(0), 1);
        Order order4 = system.placeOrder(bob, pizzaPlace, items4);
        // Try to deliver without confirming/preparing
        System.out.print("Attempt to deliver without preparing: ");
        boolean result = order4.transition(OrderState.DELIVERED);
        System.out.println(result ? "Success" : "Failed (correct!)");
        System.out.println();

        System.out.println("=== Food Delivery Demo Complete ===");
    }
}
