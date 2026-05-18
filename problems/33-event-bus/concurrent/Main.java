/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — Publisher + subscriber registration racing, verify no ConcurrentModificationException

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Event Bus Demo ===\n");

        ConcurrentEventBus bus = new ConcurrentEventBus();
        String topic = "orders";

        int publisherThreads = 5;
        int subscriberThreads = 5;
        int eventsPerPublisher = 100;
        int subscribersPerThread = 20;

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(publisherThreads + subscriberThreads);
        AtomicBoolean exceptionFound = new AtomicBoolean(false);
        AtomicInteger eventsReceived = new AtomicInteger(0);

        System.out.println("Scenario: 5 publisher threads + 5 subscriber threads racing.");
        System.out.println("  Publishers send 100 events each while subscribers register.");
        System.out.println("Expected: No ConcurrentModificationException, all events delivered.\n");

        // Subscriber threads — keep registering handlers while events fire
        for (int t = 0; t < subscriberThreads; t++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < subscribersPerThread; i++) {
                        bus.subscribe(topic, event -> eventsReceived.incrementAndGet());
                        Thread.yield(); // Increase interleaving
                    }
                } catch (ConcurrentModificationException e) {
                    exceptionFound.set(true);
                    e.printStackTrace();
                } catch (Exception e) {
                    exceptionFound.set(true);
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            }, "Subscriber-" + t).start();
        }

        // Publisher threads — publish events while handlers are being registered
        for (int t = 0; t < publisherThreads; t++) {
            final int threadId = t;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < eventsPerPublisher; i++) {
                        Event event = new Event(topic, "order-" + threadId + "-" + i);
                        bus.publish(event);
                        Thread.yield(); // Increase interleaving
                    }
                } catch (ConcurrentModificationException e) {
                    exceptionFound.set(true);
                    e.printStackTrace();
                } catch (Exception e) {
                    exceptionFound.set(true);
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            }, "Publisher-" + t).start();
        }

        startLatch.countDown();
        doneLatch.await();

        int totalEventsPublished = bus.getEventsPublished();
        int totalSubscribers = bus.getSubscriberCount(topic);

        System.out.println("--- Results ---");
        System.out.println("Events published: " + totalEventsPublished);
        System.out.println("Total subscribers registered: " + totalSubscribers);
        System.out.println("Handler invocations: " + bus.getHandlersInvoked());
        System.out.println("Events received by handlers: " + eventsReceived.get());
        System.out.println("ConcurrentModificationException: " + exceptionFound.get());

        boolean passed = !exceptionFound.get()
                && totalEventsPublished == publisherThreads * eventsPerPublisher
                && totalSubscribers == subscriberThreads * subscribersPerThread;
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}
