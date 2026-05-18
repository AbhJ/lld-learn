/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/PricingStrategy.java — Interchangeable fare calculation algorithms (base, surge)
// DESIGN PATTERN: Strategy

public interface PricingStrategy { // interface = swappable fare calculation strategy
    double calculateFare(double distanceKm, String vehicleType);
    String getName();
}

class BasePricing implements PricingStrategy {
    private static final double BASE_FARE = 3.00;
    private static final double PER_KM_RATE = 2.00;

    @Override
    public double calculateFare(double distanceKm, String vehicleType) {
        double multiplier = getVehicleMultiplier(vehicleType);
        return (BASE_FARE + distanceKm * PER_KM_RATE) * multiplier;
    }

    private double getVehicleMultiplier(String type) {
        switch (type.toLowerCase()) {
            case "economy": return 1.0;
            case "comfort": return 1.3;
            case "premium": return 1.8;
            case "xl": return 1.5;
            default: return 1.0;
        }
    }

    @Override
    public String getName() { return "Standard"; }
}

class SurgePricing implements PricingStrategy {
    private PricingStrategy basePricing;
    private double surgeMultiplier;

    public SurgePricing(PricingStrategy basePricing, double surgeMultiplier) {
        this.basePricing = basePricing;
        this.surgeMultiplier = surgeMultiplier;
    }

    @Override
    public double calculateFare(double distanceKm, String vehicleType) {
        return basePricing.calculateFare(distanceKm, vehicleType) * surgeMultiplier;
    }

    @Override
    public String getName() {
        return String.format("Surge (%.1fx)", surgeMultiplier);
    }

    public double getSurgeMultiplier() { return surgeMultiplier; }
}
