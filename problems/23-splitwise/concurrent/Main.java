/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 20 threads adding expenses among 5 users, verify net balances sum to zero

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Splitwise Demo ===\n");

        BalanceManager manager = new BalanceManager();
        String[] users = {"Alice", "Bob", "Charlie", "Diana", "Eve"};
        Set<String> userSet = new HashSet<>(Arrays.asList(users));

        int threadCount = 20;
        int expensesPerThread = 50;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger totalExpenses = new AtomicInteger(0);

        System.out.println("Scenario: 20 threads add 50 expenses each among 5 users simultaneously.");
        System.out.println("Expected: Net balances across all users always sum to zero.\n");

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            new Thread(() -> {
                try {
                    startLatch.await();
                    Random random = new Random(threadId);
                    for (int i = 0; i < expensesPerThread; i++) {
                        String payer = users[random.nextInt(users.length)];
                        // Pick 2-5 participants (always including payer)
                        List<String> participants = new ArrayList<>();
                        participants.add(payer);
                        int numOthers = 1 + random.nextInt(4);
                        for (int j = 0; j < numOthers; j++) {
                            String other = users[random.nextInt(users.length)];
                            if (!participants.contains(other)) {
                                participants.add(other);
                            }
                        }
                        long amount = (random.nextInt(100) + 1) * 100L; // $1-$100 in cents
                        Expense expense = new Expense(payer, amount, participants);
                        manager.addExpense(expense);
                        totalExpenses.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            }, "Expenser-" + t).start();
        }

        startLatch.countDown();
        doneLatch.await();

        // Verify: sum of all net balances must be zero
        long totalNet = manager.totalNetBalance(userSet);

        System.out.println("--- Results ---");
        System.out.println("Total expenses added: " + totalExpenses.get());
        for (String user : users) {
            long net = manager.getNetBalance(user);
            String status = net > 0 ? "is owed" : net < 0 ? "owes" : "settled";
            System.out.printf("  %s: %+d cents (%s)%n", user, net, status);
        }
        System.out.println("Sum of all net balances: " + totalNet);

        boolean passed = totalNet == 0;
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}
