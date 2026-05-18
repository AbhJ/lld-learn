/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 10 members try to borrow 3 available copies simultaneously, exactly 3 succeed

import model.Book;
import model.Member;
import service.Library;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Library Management Demo ===\n");

        final int NUM_MEMBERS = 10;
        final int AVAILABLE_COPIES = 3;

        Library library = new Library();
        Book book = new Book("978-0132350884", "Clean Code", "Robert C. Martin", AVAILABLE_COPIES);
        library.addBook(book);

        System.out.println("Book: " + book);
        System.out.println("Members attempting to borrow: " + NUM_MEMBERS);
        System.out.println("Available copies: " + AVAILABLE_COPIES + "\n");

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(NUM_MEMBERS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < NUM_MEMBERS; i++) {
            Member member = new Member(i, "Member-" + i);
            Thread t = new Thread(() -> {
                try {
                    startLatch.await();
                } catch (InterruptedException ignored) {}

                boolean success = library.borrowBook(member, book);
                if (success) {
                    successCount.incrementAndGet();
                    System.out.println("  [SUCCESS] " + member.getName() + " borrowed the book");
                } else {
                    failCount.incrementAndGet();
                    System.out.println("  [REJECTED] " + member.getName() + " — no copies available");
                }
                doneLatch.countDown();
            });
            threads.add(t);
            t.start();
        }

        // Release all threads simultaneously
        startLatch.countDown();
        doneLatch.await();

        System.out.println("\n--- Results ---");
        System.out.println("Book after all attempts: " + book);
        System.out.println("Available copies remaining: " + book.getAvailableCopies());

        int succeeded = successCount.get();
        int rejected = failCount.get();

        System.out.println("\n" + NUM_MEMBERS + " threads attempted, " + succeeded + " succeeded, " + rejected + " correctly rejected");

        boolean passed = (succeeded == AVAILABLE_COPIES) && (rejected == NUM_MEMBERS - AVAILABLE_COPIES)
                && (book.getAvailableCopies() == 0);
        System.out.println("Correctness check: " + (passed ? "PASSED" : "FAILED"));
    }
}
