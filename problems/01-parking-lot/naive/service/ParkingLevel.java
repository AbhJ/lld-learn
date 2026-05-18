/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ParkingLevel.java — Represents one floor of the parking structure with spot-finding logic

import java.util.ArrayList;
import java.util.List;

class ParkingLevel {
    private int levelNumber;          // private = only this class uses the level number directly
    private List<ParkingSpot> spots;  // private = external code can't tamper with the list
    private int smallCount;           // private = internal bookkeeping
    private int mediumCount;          // private = internal bookkeeping
    private int largeCount;           // private = internal bookkeeping

    public ParkingLevel(int levelNumber, int smallSpots, int mediumSpots, int largeSpots) {
        this(levelNumber, smallSpots, mediumSpots, largeSpots, new DefaultParkingSpotFactory());
    }

    /** Inject a custom factory for testing or alternative spot-construction schemes. */
    public ParkingLevel(int levelNumber, int smallSpots, int mediumSpots, int largeSpots, ParkingSpotFactory factory) {
        this.levelNumber = levelNumber;
        this.smallCount = smallSpots;
        this.mediumCount = mediumSpots;
        this.largeCount = largeSpots;
        // Factory pattern: delegate spot construction so this class doesn't know spotId conventions.
        this.spots = new ArrayList<>(factory.createSpotsForLevel(levelNumber, smallSpots, mediumSpots, largeSpots));
    }

    public synchronized ParkingSpot findAvailableSpot(SpotSize size) { // synchronized = thread-safe search
        for (ParkingSpot spot : spots) {
            if (spot.getSize() == size && spot.isAvailable()) {
                return spot;
            }
        }
        return null;
    }

    public int getAvailableCount(SpotSize size) {
        int count = 0;
        for (ParkingSpot spot : spots) {
            if (spot.getSize() == size && spot.isAvailable()) {
                count++;
            }
        }
        return count;
    }

    public int getLevelNumber() { return levelNumber; }
    public int getTotalSpots() { return spots.size(); }

    public String getAvailabilitySummary() {
        return "Level " + levelNumber + ": SMALL=" + getAvailableCount(SpotSize.SMALL) +
               ", MEDIUM=" + getAvailableCount(SpotSize.MEDIUM) +
               ", LARGE=" + getAvailableCount(SpotSize.LARGE);
    }
}
