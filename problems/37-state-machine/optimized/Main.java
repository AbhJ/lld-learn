/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates state machine with O(1) transition table lookup
public class Main {
    public static void main(String[] args) {
        System.out.println("=== State Machine (Optimized: Transition Table) Demo ===\n");

        // --- Order State Machine ---
        System.out.println("--- Order SM (O(1) lookup) ---");
        StateMachine sm = new StateMachineBuilder("Order")
                .initialState("CREATED")
                .state("PENDING").state("PAID").state("SHIPPED").state("DELIVERED").state("CANCELLED")
                .transition("CREATED", "SUBMIT", "PENDING", e -> System.out.println("    [Action] Confirming"))
                .transition("PENDING", "PAY", "PAID", e -> System.out.println("    [Action] Payment processed"))
                .transition("PAID", "SHIP", "SHIPPED")
                .transition("SHIPPED", "DELIVER", "DELIVERED")
                .transition("CREATED", "CANCEL", "CANCELLED")
                .transition("PENDING", "CANCEL", "CANCELLED")
                .build();

        sm.fire("SUBMIT");
        sm.fire("PAY");
        sm.fire("SHIP");
        sm.fire("DELIVER");
        System.out.println("  Final: " + sm.getCurrentState());
        System.out.println("  History: " + sm.getHistory());

        // --- Guarded Transitions ---
        System.out.println("\n--- Guarded Transitions ---");
        StateMachine guarded = new StateMachineBuilder("Payment")
                .initialState("PENDING")
                .state("PAID").state("FAILED")
                // Guard: payment succeeds if payload is "valid"
                .transition("PENDING", "PAY", "PAID",
                        e -> "valid".equals(e.getPayload()),
                        e -> System.out.println("    [Action] Payment OK"))
                // Fallback (no guard): if first guard fails, takes this path
                .transition("PENDING", "PAY", "FAILED")
                .build();

        guarded.fire(new Event("PAY", "valid"));
        System.out.println("  State: " + guarded.getCurrentState());

        StateMachine guarded2 = new StateMachineBuilder("Payment2")
                .initialState("PENDING")
                .state("PAID").state("FAILED")
                .transition("PENDING", "PAY", "PAID", e -> "valid".equals(e.getPayload()), null)
                .transition("PENDING", "PAY", "FAILED")
                .build();
        guarded2.fire(new Event("PAY", "invalid"));
        System.out.println("  State (invalid payment): " + guarded2.getCurrentState());

        // --- Traffic Light with Entry Actions ---
        System.out.println("\n--- Traffic Light ---");
        StateMachine light = new StateMachineBuilder("Light")
                .initialState("RED").state("GREEN").state("YELLOW")
                .onEntry("RED", e -> System.out.println("    STOP!"))
                .onEntry("GREEN", e -> System.out.println("    GO!"))
                .onEntry("YELLOW", e -> System.out.println("    CAUTION!"))
                .transition("RED", "TIMER", "GREEN")
                .transition("GREEN", "TIMER", "YELLOW")
                .transition("YELLOW", "TIMER", "RED")
                .build();
        light.fire("TIMER");
        light.fire("TIMER");
        light.fire("TIMER");
        System.out.println("  Full cycle: " + light.getHistory());

        System.out.println("\n=== State Machine (Optimized) Demo Complete ===");
    }
}
