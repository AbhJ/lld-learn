/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — Rapid GPS updates + geofence checker running, verify alerts always use latest position

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Vehicle Tracking Demo ===\n");
        System.out.println("Race condition: GPS update arriving while geofence check runs on previous location");
        System.out.println("— alert based on stale position.\n");

        Location initial = new Location(0.0, 0.0, System.nanoTime(), 0);
        VehicleTracker tracker = new VehicleTracker("VH-001", initial);

        // Geofence centered at (5.0, 5.0) with radius 1.0
        Geofence restrictedZone = new Geofence("RestrictedZone", 5.0, 5.0, 1.0);

        int gpsUpdateCount = 500;
        int geofenceChecks = 200;

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(3); // 2 updaters + 1 checker

        AtomicInteger outOfOrderAttempts = new AtomicInteger(0);

        // GPS updater thread 1: moves vehicle along a path (increasing sequence)
        new Thread(() -> {
            try {
                startLatch.await();
                for (int i = 1; i <= gpsUpdateCount; i++) {
                    double lat = (i * 10.0) / gpsUpdateCount;
                    double lon = (i * 10.0) / gpsUpdateCount;
                    Location loc = new Location(lat, lon, System.nanoTime(), i * 2);
                    tracker.updateLocation(loc);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        }).start();

        // GPS updater thread 2: tries to inject stale (out-of-order) updates
        new Thread(() -> {
            try {
                startLatch.await();
                for (int i = 1; i <= gpsUpdateCount; i++) {
                    // Odd sequence numbers — interleaved but sometimes stale
                    Location loc = new Location(0.0, 0.0, System.nanoTime(), i * 2 - 1);
                    if (!tracker.updateLocation(loc)) {
                        outOfOrderAttempts.incrementAndGet();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        }).start();

        // Geofence checker thread: continuously checks position
        List<Integer> alertSequences = new CopyOnWriteArrayList<>();
        new Thread(() -> {
            try {
                startLatch.await();
                for (int i = 0; i < geofenceChecks; i++) {
                    Location loc = tracker.getLatestLocation();
                    if (restrictedZone.contains(loc)) {
                        alertSequences.add(loc.getSequenceNum());
                    }
                    Thread.sleep(0, 100); // Tiny delay between checks
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        }).start();

        startLatch.countDown();
        doneLatch.await();

        // Verify: final location should have highest sequence number
        Location finalLoc = tracker.getLatestLocation();
        int highestPossibleSeq = gpsUpdateCount * 2;

        // Check that alerts never reference a sequence number lower than previous alert
        boolean alertsMonotonic = true;
        int prevSeq = -1;
        for (int seq : alertSequences) {
            if (seq < prevSeq) {
                alertsMonotonic = false;
                break;
            }
            prevSeq = seq;
        }

        // Check no alert used seq=0 (the stale initial position) after updates started
        boolean noStaleAlerts = true;
        for (int seq : alertSequences) {
            if (seq == 0) {
                noStaleAlerts = false;
                break;
            }
        }

        System.out.println("--- Results ---");
        System.out.println("GPS updates sent (thread 1): " + gpsUpdateCount);
        System.out.println("Stale updates sent (thread 2): " + gpsUpdateCount);
        System.out.println("Successful updates: " + tracker.getUpdateCount());
        System.out.println("Stale rejects: " + tracker.getStaleRejects());
        System.out.println("Final location seq: " + finalLoc.getSequenceNum());
        System.out.println("Highest possible seq: " + highestPossibleSeq);
        System.out.println("Geofence checks performed: " + geofenceChecks);
        System.out.println("Geofence alerts triggered: " + alertSequences.size());
        System.out.println("Out-of-order attempts blocked: " + outOfOrderAttempts.get());

        boolean finalIsHighest = (finalLoc.getSequenceNum() == highestPossibleSeq);
        boolean staleRejected = (tracker.getStaleRejects() > 0);

        System.out.println("\n--- Consistency Checks ---");
        System.out.println("Final position has highest seq: " + finalIsHighest);
        System.out.println("Stale updates were rejected: " + staleRejected);
        System.out.println("Alerts use monotonically increasing seq: " + alertsMonotonic);
        System.out.println("No stale-position alerts: " + noStaleAlerts);

        boolean passed = finalIsHighest && staleRejected && alertsMonotonic && noStaleAlerts;
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}
