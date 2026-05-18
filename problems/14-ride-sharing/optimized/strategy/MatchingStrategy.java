/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/MatchingStrategy.java — Geospatial grid + driver availability index with ConcurrentHashMap
// DESIGN PATTERN: Strategy

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public interface MatchingStrategy { // interface = swappable driver matching algorithm
    Driver findDriver(List<Driver> drivers, Location pickup);
    String getName();
}

/**
 * Optimized: Geospatial grid with ConcurrentHashMap<GridCell, List<Driver>>
 * for O(1) cell lookup instead of O(n) scan through all drivers.
 */
class GeospatialGridStrategy implements MatchingStrategy {
    private static final double CELL_SIZE_KM = 2.0; // grid cell size in km
    private ConcurrentHashMap<String, List<Driver>> grid; // ConcurrentHashMap = O(1) cell lookup by key

    public GeospatialGridStrategy() {
        this.grid = new ConcurrentHashMap<>();
    }

    private String cellKey(Location loc) {
        int cellX = (int) Math.floor(loc.getLatitude() * 111 / CELL_SIZE_KM);
        int cellY = (int) Math.floor(loc.getLongitude() * 111 * Math.cos(Math.toRadians(loc.getLatitude())) / CELL_SIZE_KM);
        return cellX + "," + cellY;
    }

    private void rebuildIndex(List<Driver> drivers) {
        grid.clear();
        for (Driver driver : drivers) {
            if (!driver.isAvailable()) continue;
            String key = cellKey(driver.getCurrentLocation());
            grid.computeIfAbsent(key, k -> new ArrayList<>()).add(driver);
        }
    }

    @Override
    public Driver findDriver(List<Driver> drivers, Location pickup) {
        rebuildIndex(drivers);

        String centerKey = cellKey(pickup);
        int centerX = Integer.parseInt(centerKey.split(",")[0]);
        int centerY = Integer.parseInt(centerKey.split(",")[1]);

        // Expanding ring search from the pickup cell outward
        for (int ring = 0; ring <= 10; ring++) {
            Driver nearest = null;
            double minDist = Double.MAX_VALUE;

            for (int dx = -ring; dx <= ring; dx++) {
                for (int dy = -ring; dy <= ring; dy++) {
                    if (ring > 0 && Math.abs(dx) != ring && Math.abs(dy) != ring) continue;
                    String key = (centerX + dx) + "," + (centerY + dy);
                    List<Driver> cellDrivers = grid.get(key);
                    if (cellDrivers == null) continue;

                    for (Driver driver : cellDrivers) {
                        double dist = driver.distanceTo(pickup);
                        if (dist < minDist) {
                            minDist = dist;
                            nearest = driver;
                        }
                    }
                }
            }
            if (nearest != null) return nearest;
        }
        return null;
    }

    @Override
    public String getName() { return "Geospatial Grid (ConcurrentHashMap)"; }
}

class HighestRatedStrategy implements MatchingStrategy {
    private double maxDistance;

    public HighestRatedStrategy(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    @Override
    public Driver findDriver(List<Driver> drivers, Location pickup) {
        Driver best = null;
        double bestRating = -1;
        for (Driver driver : drivers) {
            if (!driver.isAvailable()) continue;
            if (driver.distanceTo(pickup) > maxDistance) continue;
            if (driver.getAverageRating() > bestRating) {
                bestRating = driver.getAverageRating();
                best = driver;
            }
        }
        return best;
    }

    @Override
    public String getName() { return "Highest Rated (within " + maxDistance + " km)"; }
}
