/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates state machine with if-else transition lookup
public class Main {
    public static void main(String[] args) {
        System.out.println("=== State Machine (Naive) Demo ===\n");

        // Order lifecycle state machine
        System.out.println("--- Order State Machine ---");
        StateMachine sm = new StateMachineBuilder("Order")
                .initialState("CREATED")
                .state("PENDING").state("PAID").state("SHIPPED").state("DELIVERED").state("CANCELLED")
                .transition("CREATED", "SUBMIT", "PENDING", new Action() {
                    public void execute(Event e) { System.out.println("    [Action] Sending confirmation"); }
                    public String getDescription() { return "confirm"; }
                })
                .transition("PENDING", "PAY", "PAID")
                .transition("PAID", "SHIP", "SHIPPED")
                .transition("SHIPPED", "DELIVER", "DELIVERED")
                .transition("CREATED", "CANCEL", "CANCELLED")
                .transition("PENDING", "CANCEL", "CANCELLED")
                .build();

        sm.fire("SUBMIT");
        sm.fire("PAY");
        sm.fire("SHIP");
        sm.fire("DELIVER");
        System.out.println("  Final: " + sm.getCurrentStateName());
        System.out.println("  History: " + sm.getHistory());

        // Invalid transition
        System.out.println("\n--- Invalid Transition ---");
        StateMachine sm2 = new StateMachineBuilder("Order2")
                .initialState("CREATED").state("PENDING")
                .transition("CREATED", "SUBMIT", "PENDING")
                .build();
        sm2.fire("DELIVER");

        // Traffic light with entry actions
        System.out.println("\n--- Traffic Light ---");
        StateMachine light = new StateMachineBuilder("Light")
                .initialState("RED").state("GREEN").state("YELLOW")
                .onEntry("RED", new Action() { public void execute(Event e) { System.out.println("    STOP!"); } public String getDescription() { return ""; } })
                .onEntry("GREEN", new Action() { public void execute(Event e) { System.out.println("    GO!"); } public String getDescription() { return ""; } })
                .transition("RED", "TIMER", "GREEN")
                .transition("GREEN", "TIMER", "YELLOW")
                .transition("YELLOW", "TIMER", "RED")
                .build();
        light.fire("TIMER");
        light.fire("TIMER");
        light.fire("TIMER");

        System.out.println("\n=== State Machine (Naive) Demo Complete ===");
    }
}
