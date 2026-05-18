/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates connection pool with synchronized ArrayList
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Connection Pool Demo (Naive) ===\n");

        ConnectionPool pool = new ConnectionPool(new PoolConfig(2, 5, 3000));
        System.out.println("Pool created (min=2, max=5)");
        System.out.println("Initial: " + pool.getStats());

        System.out.println("\n--- Borrow/Return (synchronized ArrayList) ---");
        Connection c1 = pool.borrow();
        System.out.println("Borrowed: " + c1.getId() + " → " + pool.getStats());
        Connection c2 = pool.borrow();
        System.out.println("Borrowed: " + c2.getId() + " → " + pool.getStats());
        Connection c3 = pool.borrow();
        System.out.println("Borrowed: " + c3.getId() + " (new) → " + pool.getStats());

        System.out.println("\n--- Using Connection ---");
        System.out.println(c1.execute("SELECT * FROM users"));

        System.out.println("\n--- Return and Reuse ---");
        pool.returnConnection(c1);
        System.out.println("Returned " + c1.getId() + " → " + pool.getStats());
        Connection c4 = pool.borrow();
        System.out.println("Borrowed: " + c4.getId() + " (reused) → " + pool.getStats());

        pool.returnConnection(c2);
        pool.returnConnection(c3);
        pool.returnConnection(c4);

        System.out.println("\n--- Shutdown ---");
        pool.shutdown();
        System.out.println("Pool shut down");
        try { pool.borrow(); } catch (IllegalStateException e) { System.out.println("Borrow after shutdown: " + e.getMessage()); }

        System.out.println("\n=== Demo Complete ===");
    }
}
