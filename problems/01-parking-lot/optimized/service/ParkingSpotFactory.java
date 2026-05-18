/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ParkingSpotFactory.java — Factory pattern: encapsulates ParkingSpot creation rules

import java.util.ArrayList;
import java.util.List;

/**
 * Factory pattern: centralizes the construction of ParkingSpot objects.
 * Callers do not call `new ParkingSpot(...)` directly — they request a configured
 * batch of spots for a level. This keeps the spotId scheme (e.g. "L2-S0", "L2-M0",
 * "L2-LG0") in one place, and lets us swap the implementation (e.g. read from a
 * config file, randomize, or pre-warm a pool) without changing callers.
 */
interface ParkingSpotFactory {
    /** Create all spots for a single level given the desired counts per size. */
    List<ParkingSpot> createSpotsForLevel(int level, int smallCount, int mediumCount, int largeCount);
}

/** Default factory: produces spots with a deterministic naming scheme. */
class DefaultParkingSpotFactory implements ParkingSpotFactory {
    @Override
    public List<ParkingSpot> createSpotsForLevel(int level, int smallCount, int mediumCount, int largeCount) {
        List<ParkingSpot> spots = new ArrayList<>(smallCount + mediumCount + largeCount);
        for (int i = 0; i < smallCount; i++) {
            spots.add(createSpot(level, SpotSize.SMALL, "S", i));
        }
        for (int i = 0; i < mediumCount; i++) {
            spots.add(createSpot(level, SpotSize.MEDIUM, "M", i));
        }
        for (int i = 0; i < largeCount; i++) {
            spots.add(createSpot(level, SpotSize.LARGE, "LG", i));
        }
        return spots;
    }

    private ParkingSpot createSpot(int level, SpotSize size, String prefix, int index) {
        String spotId = "L" + level + "-" + prefix + index;
        return new ParkingSpot(spotId, size, level);
    }
}
