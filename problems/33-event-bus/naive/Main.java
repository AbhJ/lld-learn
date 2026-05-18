/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates event bus with synchronous ArrayList-based dispatch
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Event Bus (Naive) Demo ===\n");

        // --- Test 1: Basic Pub/Sub ---
        System.out.println("--- Test 1: Basic Publish/Subscribe ---");
        EventBus bus = new EventBus();
        bus.subscribe(new EventHandler() {
            public void handle(Event event) { System.out.println("  UserHandler: " + event); }
            public String getName() { return "UserHandler"; }
        }, UserEvent.class);

        bus.publish(new UserEvent("LOGIN", "alice"));
        bus.publish(new UserEvent("LOGIN", "bob"));
        bus.publish(new SystemEvent("STARTUP", "booted")); // no handler -> dead letter

        // --- Test 2: Priority ---
        System.out.println("\n--- Test 2: Priority Handlers ---");
        EventBus priorityBus = new EventBus();
        priorityBus.subscribe(new EventHandler() {
            public void handle(Event e) { System.out.println("  [LOW-1]"); }
            public String getName() { return "Low"; }
        }, Event.class, 1);
        priorityBus.subscribe(new EventHandler() {
            public void handle(Event e) { System.out.println("  [HIGH-10]"); }
            public String getName() { return "High"; }
        }, Event.class, 10);
        priorityBus.subscribe(new EventHandler() {
            public void handle(Event e) { System.out.println("  [MED-5]"); }
            public String getName() { return "Med"; }
        }, Event.class, 5);
        priorityBus.publish(new Event("TEST"));

        // --- Test 3: Dead Letter Queue ---
        System.out.println("\n--- Test 3: Dead Letter Queue ---");
        EventBus dlqBus = new EventBus();
        dlqBus.publish(new Event("ORPHAN_1"));
        dlqBus.publish(new Event("ORPHAN_2"));
        System.out.println("  Dead letters: " + dlqBus.getDeadLetterQueue().size());

        // --- Test 4: Filtering ---
        System.out.println("\n--- Test 4: Event Filtering ---");
        EventBus filterBus = new EventBus();
        filterBus.subscribe(new EventHandler() {
            public void handle(Event e) { System.out.println("  VIP: " + ((UserEvent)e).getUsername()); }
            public String getName() { return "VIP"; }
        }, UserEvent.class, 10, event -> event instanceof UserEvent && ((UserEvent)event).getUsername().equals("admin"));
        filterBus.subscribe(new EventHandler() {
            public void handle(Event e) { System.out.println("  All: " + ((UserEvent)e).getUsername()); }
            public String getName() { return "All"; }
        }, UserEvent.class, 1);

        filterBus.publish(new UserEvent("ACTION", "regular"));
        filterBus.publish(new UserEvent("ACTION", "admin"));

        System.out.println("\n=== Event Bus (Naive) Demo Complete ===");
    }
}
