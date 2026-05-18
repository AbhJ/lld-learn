/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 50 threads requesting from pool of 10, demonstrates proper blocking and timeout

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Connection Pool Demo ===\n");

        int poolSize = 10;
        long timeoutMs = 500;
        int threadCount = 50;

        ConcurrentConnectionPool pool = new ConcurrentConnectionPool(poolSize, timeoutMs);

        CountDownLatch startLatch = new CountDownLatch(1);    // CountDownLatch = barrier; all threads start simultaneously
        CountDownLatch doneLatch = new CountDownLatch(threadCount); // counts down as each thread finishes
        AtomicInteger acquired = new AtomicInteger(0);       // AtomicInteger = thread-safe success counter
        AtomicInteger timedOut = new AtomicInteger(0);       // AtomicInteger = thread-safe timeout counter
        AtomicInteger errors = new AtomicInteger(0);
        List<String> log = Collections.synchronizedList(new ArrayList<>()); // synchronizedList = thread-safe appends

        System.out.println("Scenario: 50 threads request connections from a pool of 10.");
        System.out.println("Each thread holds the connection for 200ms (simulating work).");
        System.out.println("Timeout: " + timeoutMs + "ms\n");

        for (int i = 0; i < threadCount; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    Connection conn = pool.acquire();
                    if (conn != null) {
                        acquired.incrementAndGet();
                        try {
                            // Simulate doing work with the connection
                            String result = conn.execute("SELECT * FROM users WHERE id=" + id);
                            Thread.sleep(200); // Hold connection for 200ms
                        } finally {
                            pool.release(conn);
                        }
                    } else {
                        timedOut.incrementAndGet();
                        log.add("  [TIMEOUT] Thread-" + id + " could not get connection within " + timeoutMs + "ms");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    errors.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            }, "Worker-" + id).start();
        }

        startLatch.countDown();
        doneLatch.await();

        // Print timeout logs (first 5)
        int shown = 0;
        for (String entry : log) {
            if (shown++ >= 5) {
                System.out.println("  ... and " + (log.size() - 5) + " more timeouts");
                break;
            }
            System.out.println(entry);
        }

        System.out.println("\n--- Summary ---");
        System.out.println("Pool size: " + poolSize);
        System.out.println("Threads attempted: " + threadCount);
        System.out.println("Successfully acquired: " + acquired.get());
        System.out.println("Timed out: " + timedOut.get());
        System.out.println("Errors: " + errors.get());
        System.out.println("Final pool state: active=" + pool.getActiveCount() + ", idle=" + pool.getIdleCount());

        // Verify: no connection leaks
        boolean noLeaks = pool.getActiveCount() == 0;
        boolean allAccountedFor = acquired.get() + timedOut.get() == threadCount;
        System.out.println("\nNo connection leaks: " + (noLeaks ? "PASSED" : "FAILED"));
        System.out.println("All threads accounted for: " + (allAccountedFor ? "PASSED" : "FAILED"));

        pool.shutdown();
    }
}
