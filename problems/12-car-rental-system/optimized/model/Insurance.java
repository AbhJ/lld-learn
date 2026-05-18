/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Insurance.java — Insurance options with coverage details and daily rates

public class Insurance {
    private String type;            // insurance type name (Basic, Premium, Liability)
    private String description;     // what the insurance covers
    private double dailyRate;       // per-day cost; multiplied by rental days

    public Insurance(String type, String description, double dailyRate) {
        this.type = type;
        this.description = description;
        this.dailyRate = dailyRate;
    }

    public String getType() { return type; }
    public String getDescription() { return description; }
    public double getDailyRate() { return dailyRate; }

    public double getCost(int days) {
        return dailyRate * days;
    }

    @Override
    public String toString() {
        return String.format("%s ($%.2f/day): %s", type, dailyRate, description);
    }

    // Predefined insurance options
    public static Insurance basic() {
        return new Insurance("Basic", "Covers collision damage", 10.00);
    }

    public static Insurance premium() {
        return new Insurance("Premium", "Full coverage including theft and personal items", 25.00);
    }

    public static Insurance liability() {
        return new Insurance("Liability", "Third-party liability coverage", 8.00);
    }
}
