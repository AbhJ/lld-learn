/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/ParkingLevel.java — ConcurrentLinkedQueue + AtomicInteger for thread-safe level management

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

class ParkingLevel {
    private final int levelNumber;    // final = immutable identity; safe publication to all threads
    private final Map<SpotSize, ConcurrentLinkedQueue<ParkingSpot>> availableSpots; // ConcurrentLinkedQueue = lock-free poll gives each thread a unique spot
    private final Map<SpotSize, AtomicInteger> availableCounts; // AtomicInteger = lock-free counter; no lost updates

    public ParkingLevel(int levelNumber, int smallSpots, int mediumSpots, int largeSpots) {
        this.levelNumber = levelNumber;
        this.availableSpots = new EnumMap<>(SpotSize.class);
        this.availableCounts = new EnumMap<>(SpotSize.class);

        for (SpotSize size : SpotSize.values()) {
            availableSpots.put(size, new ConcurrentLinkedQueue<>());
            availableCounts.put(size, new AtomicInteger(0));
        }

        initSpots(SpotSize.SMALL, smallSpots, "S");
        initSpots(SpotSize.MEDIUM, mediumSpots, "M");
        initSpots(SpotSize.LARGE, largeSpots, "LG");
    }

    private void initSpots(SpotSize size, int count, String prefix) {
        for (int i = 0; i < count; i++) {
            ParkingSpot spot = new ParkingSpot("L" + levelNumber + "-" + prefix + i, size, levelNumber);
            availableSpots.get(size).offer(spot);
            availableCounts.get(size).incrementAndGet();
        }
    }

    /**
     * Thread-safe spot retrieval using ConcurrentLinkedQueue.poll().
     * Multiple threads can safely call this simultaneously — each gets a unique spot or null.
     */
    public ParkingSpot findAvailableSpot(SpotSize size) {
        ParkingSpot spot = availableSpots.get(size).poll();
        if (spot != null) {
            availableCounts.get(size).decrementAndGet();
        }
        return spot;
    }

    /**
     * Return a spot to the available pool after unpark.
     */
    public void returnSpot(ParkingSpot spot) {
        availableSpots.get(spot.getSize()).offer(spot);
        availableCounts.get(spot.getSize()).incrementAndGet();
    }

    public int getAvailableCount(SpotSize size) {
        return availableCounts.get(size).get();
    }

    public int getLevelNumber() { return levelNumber; }

    public int getTotalAvailable() {
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
