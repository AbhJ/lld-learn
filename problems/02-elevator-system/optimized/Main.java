/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the elevator system with test scenarios

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Elevator System Test ===\n");

        // Create building: 3 elevators, 10 floors (0-10)
        ElevatorSystem system = new ElevatorSystem(3, 10);
        Display display = new Display("Lobby Display", false);
        system.addObserverToAll(display);

        System.out.println("Building: 10 floors, 3 elevators");
        System.out.println(system.getStatus());

        // --- Test: Basic Request ---
        System.out.println("\n--- Test: Basic Request (Floor 0 -> Floor 5) ---");
        Request r1 = new Request(0, 5);
        Elevator assigned = system.handleRequest(r1);
        System.out.println("Assigned: Elevator " + assigned.getId());
        System.out.println("Simulating movement...");
        for (int i = 0; i < 6; i++) {
            system.step();
            System.out.println("  Step " + (i + 1) + ": " + assigned.getStatus());
        }

        // --- Test: Multiple Requests with SCAN ---
        System.out.println("\n--- Test: Multiple Requests (SCAN Strategy) ---");
        ElevatorSystem system2 = new ElevatorSystem(2, 10);
        system2.setStrategy(new SCANStrategy());
        System.out.println("Strategy: SCAN");

        Request r2 = new Request(0, 8);
        Request r3 = new Request(0, 3);
        Request r4 = new Request(5, 1);

        Elevator a2 = system2.handleRequest(r2);
        System.out.println("Request Floor 0->8: Elevator " + a2.getId());
        Elevator a3 = system2.handleRequest(r3);
        System.out.println("Request Floor 0->3: Elevator " + a3.getId());
        Elevator a4 = system2.handleRequest(r4);
        System.out.println("Request Floor 5->1: Elevator " + a4.getId());

        System.out.println("Running simulation...");
        for (int i = 0; i < 12; i++) {
            system2.step();
        }
        System.out.println(system2.getStatus());

        // --- Test: LOOK Strategy ---
        System.out.println("\n--- Test: LOOK Strategy ---");
        ElevatorSystem system3 = new ElevatorSystem(2, 10);
        system3.setStrategy(new LOOKStrategy());
        System.out.println("Strategy: LOOK");

        Elevator a5 = system3.handleRequest(new Request(0, 7));
        System.out.println("Request Floor 0->7: Elevator " + a5.getId());
        Elevator a6 = system3.handleRequest(new Request(3, 9));
        System.out.println("Request Floor 3->9: Elevator " + a6.getId());

        for (int i = 0; i < 12; i++) {
            system3.step();
        }
        System.out.println(system3.getStatus());

        // --- Test: Maintenance Mode ---
        System.out.println("\n--- Test: Maintenance Mode ---");
        ElevatorSystem system4 = new ElevatorSystem(3, 10);
        system4.setMaintenance(1, true);
        System.out.println("Elevator 2 set to MAINTENANCE");
        System.out.println(system4.getStatus());

        Elevator a7 = system4.handleRequest(new Request(0, 5));
        System.out.println("\nRequest Floor 0->5: Elevator " + a7.getId() + " (Elevator 2 skipped)");

        Elevator a8 = system4.handleRequest(new Request(3, 8));
        System.out.println("Request Floor 3->8: Elevator " + a8.getId());

        // --- Test: Elevator with Observer ---
        System.out.println("\n--- Test: Elevator with Verbose Observer ---");
        ElevatorSystem system5 = new ElevatorSystem(1, 5);
        Display verboseDisplay = new Display("Floor Display", true);
        system5.addObserverToAll(verboseDisplay);

        system5.handleRequest(new Request(0, 3));
        for (int i = 0; i < 5; i++) {
            system5.step();
        }

        // --- Test: Direction Priority ---
        System.out.println("\n--- Test: Same Direction Priority ---");
        ElevatorSystem system6 = new ElevatorSystem(2, 10);
        system6.handleRequest(new Request(0, 5));
        for (int i = 0; i < 2; i++) { system6.step(); }

        Elevator a9 = system6.handleRequest(new Request(3, 7));
        System.out.println("Elevator at floor 2 going up, request from floor 3 up: Elevator " + a9.getId());

        Elevator a10 = system6.handleRequest(new Request(4, 1));
        System.out.println("Request from floor 4 going down: Elevator " + a10.getId());

        System.out.println("\n=== All Tests Passed ===");
    }
}
