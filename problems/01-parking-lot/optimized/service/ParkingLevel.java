/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ParkingLevel.java — O(1) spot lookup using pre-bucketed queues and atomic counters

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

class ParkingLevel {
    private final int levelNumber;    // final = set once; level identity never changes
    // O(1) spot retrieval — pre-bucketed by size
    private final Map<SpotSize, Queue<ParkingSpot>> availableSpots; // ConcurrentLinkedQueue = O(1) poll vs O(n) linear scan
    private final Map<SpotSize, AtomicInteger> availableCounts;     // AtomicInteger = O(1) count vs recounting every time

    public ParkingLevel(int levelNumber, int smallSpots, int mediumSpots, int largeSpots) {
        this(levelNumber, smallSpots, mediumSpots, largeSpots, new DefaultParkingSpotFactory());
    }

    /** Inject a custom factory for testing or alternative spot-construction schemes. */
    public ParkingLevel(int levelNumber, int smallSpots, int mediumSpots, int largeSpots, ParkingSpotFactory factory) {
        this.levelNumber = levelNumber;
        this.availableSpots = new EnumMap<>(SpotSize.class);
        this.availableCounts = new EnumMap<>(SpotSize.class);

        for (SpotSize size : SpotSize.values()) {
            availableSpots.put(size, new ConcurrentLinkedQueue<>());
            availableCounts.put(size, new AtomicInteger(0));
        }

        // Factory pattern: delegate spot construction; bucket the spots into the per-size queues.
        for (ParkingSpot spot : factory.createSpotsForLevel(levelNumber, smallSpots, mediumSpots, largeSpots)) {
            availableSpots.get(spot.getSize()).offer(spot);
            availableCounts.get(spot.getSize()).incrementAndGet();
        }
    }

    // O(1) — just poll from the queue!
    public ParkingSpot findAvailableSpot(SpotSize size) {
        ParkingSpot spot = availableSpots.get(size).poll();
        if (spot != null) {
            availableCounts.get(size).decrementAndGet();
        }
        return spot;
    }

    // Return a spot to the available pool after unpark
    public void returnSpot(ParkingSpot spot) {
        availableSpots.get(spot.getSize()).offer(spot);
        availableCounts.get(spot.getSize()).incrementAndGet();
    }

    // O(1) — just read the counter
    public int getAvailableCount(SpotSize size) {
        return availableCounts.get(size).get();
    }

    public int getLevelNumber() { return levelNumber; }

    public int getTotalSpots() {
        int total = 0;
        for (AtomicInteger count : availableCounts.values()) {
            total += count.get();
        }
        return total;
    }

    public String getAvailabilitySummary() {
        return "Level " + levelNumber + ": SMALL=" + getAvailableCount(SpotSize.SMALL) +
               ", MEDIUM=" + getAvailableCount(SpotSize.MEDIUM) +
               ", LARGE=" + getAvailableCount(SpotSize.LARGE);
    }
}
