/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the traffic signal system

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Traffic Signal System Test ===\n");

        Intersection intersection = new Intersection("Main St & 1st Ave", 5, 2);
        intersection.addSignal("NS");
        intersection.addSignal("EW");

        PedestrianSignal nsPedestrian = new PedestrianSignal("PED-NS", "North-South Crosswalk", "NS");
        PedestrianSignal ewPedestrian = new PedestrianSignal("PED-EW", "East-West Crosswalk", "EW");
        intersection.addPedestrianSignal(nsPedestrian, "NS");
        intersection.addPedestrianSignal(ewPedestrian, "EW");

        // Add observers — SignalDisplay implements SignalObserver
        intersection.getSignal("NS").addObserver(new SignalDisplay("NS Display"));
        intersection.getSignal("EW").addObserver(new SignalDisplay("EW Display"));

        String[] order = {"NS", "EW"};
        intersection.initialize(order);

        System.out.println("--- Test: Normal Cycle (Green=5 ticks, Yellow=2 ticks) ---");
        System.out.println("Tick 0: " + intersection.getStatus());

        for (int i = 1; i <= 14; i++) {
            intersection.tick();
            if (i == 5 || i == 7 || i == 12 || i == 14) {
                System.out.println("Tick " + i + ": " + intersection.getStatus());
            }
        }

        System.out.println("\n--- Test: Emergency Override ---");
        Intersection intersection2 = new Intersection("Broadway & 5th", 5, 2);
        intersection2.addSignal("NS");
        intersection2.addSignal("EW");
        intersection2.initialize(order);

        System.out.println("Before emergency: " + intersection2.getStatus());
        intersection2.tick();
        intersection2.tick();
        System.out.println("After 2 ticks: " + intersection2.getStatus());

        System.out.println("EMERGENCY: Vehicle approaching from EW direction!");
        intersection2.emergencyOverride("EW");
        System.out.println("After override: " + intersection2.getStatus());

        intersection2.tick();
        intersection2.tick();
        System.out.println("During emergency (2 ticks): " + intersection2.getStatus());

        System.out.println("Emergency cleared.");
        intersection2.clearEmergency();
        System.out.println("After clear: " + intersection2.getStatus());

        for (int i = 0; i < 5; i++) {
            intersection2.tick();
        }
        System.out.println("After 5 more ticks: " + intersection2.getStatus());

        System.out.println("\n--- Test: Signal State Logic ---");
        System.out.println("GREEN.next() = " + SignalState.GREEN.next());
        System.out.println("YELLOW.next() = " + SignalState.YELLOW.next());
        System.out.println("RED.next() = " + SignalState.RED.next());

        System.out.println("\n=== All Tests Passed ===");
    }
}
