/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/SpeedMonitor.java — Monitors vehicle speed and detects violations
public class SpeedMonitor {
    private double speedLimitKmh; // private = configurable speed limit threshold

    public SpeedMonitor(double speedLimitKmh) { this.speedLimitKmh = speedLimitKmh; }
    public double getSpeedLimitKmh() { return speedLimitKmh; }
    public void setSpeedLimitKmh(double limit) { this.speedLimitKmh = limit; }

    public double calculateSpeed(Location from, Location to, double timeDeltaSeconds) {
        if (timeDeltaSeconds <= 0) return 0;
        double distanceMeters = from.distanceTo(to);
        return (distanceMeters / timeDeltaSeconds) * 3.6;
    }

    public boolean isExceedingLimit(double speedKmh) { return speedKmh > speedLimitKmh; }
}
