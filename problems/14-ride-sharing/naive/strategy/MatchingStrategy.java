/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/MatchingStrategy.java — Interchangeable driver matching algorithms (nearest, highest rated)
// DESIGN PATTERN: Strategy

import java.util.List;

public interface MatchingStrategy { // interface = contract for driver matching algorithms
    Driver findDriver(List<Driver> drivers, Location pickup);
    String getName();
}

class NearestDriverStrategy implements MatchingStrategy { // implements = fulfills the contract
    @Override
    public Driver findDriver(List<Driver> drivers, Location pickup) {
        Driver nearest = null;
        double minDist = Double.MAX_VALUE;

        for (Driver driver : drivers) {
            if (!driver.isAvailable()) continue;
            double dist = driver.distanceTo(pickup);
            if (dist < minDist) {
                minDist = dist;
                nearest = driver;
            }
        }
        return nearest;
    }

    @Override
    public String getName() { return "Nearest Driver"; }
}

class HighestRatedStrategy implements MatchingStrategy { // implements = fulfills the contract
    private double maxDistance;    // private = radius filter for candidate drivers

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
