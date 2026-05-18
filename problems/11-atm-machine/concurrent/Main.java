/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 10 threads withdrawing $100 from $500 account, exactly 5 succeed

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent ATM Machine Demo ===\n");

        Account account = new Account("ACC-001", "Alice", 500);
        Card card = new Card("4111111111114321", "1234", account);
        ATM atm = new ATM("ATM-01");

        int threadCount = 10;
        long withdrawalAmount = 100;

        System.out.println("Scenario: 10 threads each withdrawing $" + withdrawalAmount +
                " from account with $" + account.getBalanceDollars() + " balance.");
        System.out.println("Expected: Exactly 5 succeed (5 x $100 = $500), 5 fail (insufficient funds).\n");

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    boolean success = atm.withdraw(account, withdrawalAmount);
                    if (success) {
                        successCount.incrementAndGet();
                        results.add("  [SUCCESS] Thread-" + id + " withdrew $" + withdrawalAmount +
                                " | Balance after: $" + account.getBalanceDollars());
                    } else {
                        failCount.incrementAndGet();
                        results.add("  [FAILED]  Thread-" + id + " insufficient funds" +
                                " | Balance: $" + account.getBalanceDollars());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }, "ATM-Thread-" + id).start();
        }

        // Release all threads simultaneously
        startLatch.countDown();
        doneLatch.await();

        // Print results
        results.sort(String::compareTo);
        for (String r : results) {
            System.out.println(r);
        }

        // Verification
        System.out.println("\n--- Summary ---");
        System.out.println("Initial balance: $500");
        System.out.println("Withdrawal amount: $" + withdrawalAmount + " x " + threadCount + " threads");
        System.out.println("Successful withdrawals: " + successCount.get());
        System.out.println("Failed withdrawals: " + failCount.get());
        System.out.println("Final balance: $" + account.getBalanceDollars());

        boolean correctCount = successCount.get() == 5 && failCount.get() == 5;
        boolean correctBalance = account.getBalanceDollars() == 0;
        boolean noOverdraft = account.getBalanceCents() >= 0;

        System.out.println("\nExactly 5 succeeded: " + (correctCount ? "PASSED" : "FAILED"));
        System.out.println("Final balance is $0: " + (correctBalance ? "PASSED" : "FAILED"));
        System.out.println("No overdraft (balance >= 0): " + (noOverdraft ? "PASSED" : "FAILED"));

        boolean allPassed = correctCount && correctBalance && noOverdraft;
        System.out.println("\nOverall: " + (allPassed ? "ALL TESTS PASSED" : "SOME TESTS FAILED"));
    }
}
