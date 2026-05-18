/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Insurance.java — Insurance options with coverage details and daily rates

public class Insurance {
    private String type;            // private = encapsulates insurance type name
    private String description;     // private = encapsulates what the insurance covers
    private double dailyRate;       // private = encapsulates pricing per day

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
    public static Insurance basic() { // static = factory method; no instance needed to call
        return new Insurance("Basic", "Covers collision damage", 10.00);
    }

    public static Insurance premium() { // static = factory method; creates preset insurance
        return new Insurance("Premium", "Full coverage including theft and personal items", 25.00);
    }

    public static Insurance liability() { // static = factory method; creates preset insurance
        return new Insurance("Liability", "Third-party liability coverage", 8.00);
    }
}
