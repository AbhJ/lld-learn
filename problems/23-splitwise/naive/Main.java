/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates expense splitting with multiple strategies
import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Splitwise Demo (Naive) ===\n");

        SplitwiseService service = new SplitwiseService();
        User alice = service.addUser("u1", "Alice", "alice@email.com");
        User bob = service.addUser("u2", "Bob", "bob@email.com");
        User charlie = service.addUser("u3", "Charlie", "charlie@email.com");
        User diana = service.addUser("u4", "Diana", "diana@email.com");

        Group trip = service.createGroup("g1", "Weekend Trip", Arrays.asList(alice, bob, charlie, diana));
        System.out.println("Group: " + trip);

        System.out.println("\n--- Adding Expenses ---");
        service.addGroupExpense("g1", "e1", "Dinner", 120.0, alice, new SplitStrategy.EqualSplit(), new HashMap<>());
        System.out.println("Dinner $120 paid by Alice, split equally");

        service.addGroupExpense("g1", "e2", "Cab", 40.0, bob, new SplitStrategy.EqualSplit(), new HashMap<>());
        System.out.println("Cab $40 paid by Bob, split equally");

        Map<User, Double> hotelSplit = new HashMap<>();
        hotelSplit.put(alice, 40.0); hotelSplit.put(bob, 30.0);
        hotelSplit.put(charlie, 20.0); hotelSplit.put(diana, 10.0);
        service.addGroupExpense("g1", "e3", "Hotel", 200.0, charlie, new SplitStrategy.PercentageSplit(), hotelSplit);
        System.out.println("Hotel $200 paid by Charlie, split by percentage");

        System.out.println("\n--- Pairwise Balances (naive: tracks all pairs) ---");
        for (Balance b : service.getBalances()) {
            System.out.println("  " + b);
        }

        System.out.println("\n--- Settlements ---");
        for (Settlement s : service.getSettlements()) {
            System.out.println("  " + s);
        }

        System.out.println("\n=== Demo Complete ===");
    }
}
