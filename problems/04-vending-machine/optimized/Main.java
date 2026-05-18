/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the vending machine with test scenarios

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Vending Machine Test ===\n");

        VendingMachine machine = new VendingMachine(new GreedyChangeStrategy());
        machine.addObserver(new LoggingObserver());

        Product cola = new Product("A1", "Cola", 50);
        Product chips = new Product("A2", "Chips", 75);
        Product candy = new Product("A3", "Candy", 35);
        Product water = new Product("A4", "Water", 100);

        machine.getInventory().addProduct(cola, 5);
        machine.getInventory().addProduct(chips, 3);
        machine.getInventory().addProduct(candy, 10);
        machine.getInventory().addProduct(water, 1);

        System.out.println("Machine stocked:");
        System.out.println(machine.getInventory().getDisplayInfo());

        System.out.println("\n--- Test: Exact Change Purchase (Cola $0.50) ---");
        machine.insertCoin(Coin.QUARTER);
        machine.insertCoin(Coin.QUARTER);
        machine.selectProduct("A1");

        System.out.println("\n--- Test: Purchase with Change (Chips $0.75) ---");
        machine.insertCoin(Coin.QUARTER);
        machine.insertCoin(Coin.QUARTER);
        machine.insertCoin(Coin.QUARTER);
        machine.insertCoin(Coin.QUARTER);
        machine.selectProduct("A2");

        System.out.println("\n--- Test: Insufficient Funds ---");
        machine.insertCoin(Coin.DIME);
        machine.selectProduct("A2");
        machine.insertCoin(Coin.QUARTER);
        machine.insertCoin(Coin.QUARTER);
        machine.insertCoin(Coin.QUARTER);
        machine.selectProduct("A2");

        System.out.println("\n--- Test: Cancel and Refund ---");
        machine.insertCoin(Coin.QUARTER);
        machine.insertCoin(Coin.DIME);
        System.out.println("  Status: " + machine.getStatus());
        machine.cancel();
        System.out.println("  Status after cancel: " + machine.getStatus());

        System.out.println("\n--- Test: Select Without Money ---");
        machine.selectProduct("A1");

        System.out.println("\n--- Test: Mixed Coins (Candy $0.35) ---");
        machine.insertCoin(Coin.QUARTER);
        machine.insertCoin(Coin.DIME);
        machine.selectProduct("A3");

        System.out.println("\n--- Test: Buy Last Item (Water $1.00) ---");
        machine.insertCoin(Coin.QUARTER);
        machine.insertCoin(Coin.QUARTER);
        machine.insertCoin(Coin.QUARTER);
        machine.insertCoin(Coin.QUARTER);
        machine.selectProduct("A4");

        System.out.println("\n--- Test: Sold Out ---");
        machine.insertCoin(Coin.QUARTER);
        machine.insertCoin(Coin.QUARTER);
        machine.insertCoin(Coin.QUARTER);
        machine.insertCoin(Coin.QUARTER);
        machine.selectProduct("A4");
        machine.cancel();

        System.out.println("\n--- Final Inventory ---");
        System.out.println(machine.getInventory().getDisplayInfo());

        System.out.println("\n=== All Tests Passed ===");
    }
}
