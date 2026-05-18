/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates event bus with per-type handlers and async CompletableFuture dispatch
public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Event Bus (Optimized: Per-Type + Async) Demo ===\n");

        // --- Test 1: Type-Safe Subscribe ---
        System.out.println("--- Test 1: Type-Safe Subscribe ---");
        EventBus bus = new EventBus();

        EventHandler<UserEvent> userHandler = new EventHandler<UserEvent>() {
            public void handle(UserEvent event) { System.out.println("  UserHandler: " + event); }
            public String getName() { return "UserHandler"; }
        };
        EventHandler<Event> allHandler = new EventHandler<Event>() {
            public void handle(Event event) { System.out.println("  AllHandler: " + event); }
            public String getName() { return "AllHandler"; }
        };

        bus.subscribe(UserEvent.class, userHandler);
        bus.subscribe(Event.class, allHandler);

        bus.publish(new UserEvent("LOGIN", "alice"));
        bus.publish(new SystemEvent("BOOT", "started"));

        // --- Test 2: Priority ---
        System.out.println("\n--- Test 2: Priority ---");
        EventBus pBus = new EventBus();
        pBus.subscribe(Event.class, new EventHandler<Event>() {
            public void handle(Event e) { System.out.println("  [LOW-1]"); }
            public String getName() { return "Low"; }
        }, 1);
        pBus.subscribe(Event.class, new EventHandler<Event>() {
            public void handle(Event e) { System.out.println("  [HIGH-10]"); }
            public String getName() { return "High"; }
        }, 10);
        pBus.publish(new Event("TEST"));

        // --- Test 3: Async Dispatch ---
        System.out.println("\n--- Test 3: Async Dispatch (CompletableFuture) ---");
        EventBus asyncBus = new EventBus();
        asyncBus.setAsyncMode(true);
        asyncBus.subscribe(Event.class, new EventHandler<Event>() {
            public void handle(Event event) {
                System.out.println("  [Async] " + Thread.currentThread().getName() + ": " + event);
            }
            public String getName() { return "AsyncHandler"; }
        });
        asyncBus.publish(new Event("ASYNC_1"));
        asyncBus.publish(new Event("ASYNC_2"));
        asyncBus.publish(new Event("ASYNC_3"));
        Thread.sleep(200);

        // --- Test 4: Dead Letter Queue ---
        System.out.println("\n--- Test 4: Dead Letter Queue ---");
        EventBus dlqBus = new EventBus();
        dlqBus.publish(new Event("ORPHAN"));
        System.out.println("  Dead letters: " + dlqBus.getDeadLetters().size());

        // --- Test 5: Unsubscribe ---
        System.out.println("\n--- Test 5: Unsubscribe ---");
        System.out.println("  Before: " + bus.getSubscriptionCount());
        bus.unsubscribe(userHandler);
        System.out.println("  After: " + bus.getSubscriptionCount());

        System.out.println("\n=== Event Bus (Optimized) Demo Complete ===");
    }
}
