/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/PricingStrategy.java — Interchangeable pricing algorithms (same as naive, strategy pattern is already clean)
// DESIGN PATTERN: Strategy

interface PricingStrategy {            // interface = contract; any pricing class MUST define these
    double calculateFee(Ticket ticket);
    String getName();
}

class HourlyPricing implements PricingStrategy { // implements = fulfills PricingStrategy contract
    private final double ratePerHour; // final = rate set once at construction, never changes

    public HourlyPricing(double ratePerHour) {
        if (ratePerHour < 0) throw new IllegalArgumentException("Rate cannot be negative");
        this.ratePerHour = ratePerHour;
    }

    @Override
    public double calculateFee(Ticket ticket) {
        return ticket.getDurationInHours() * ratePerHour;
    }

    @Override
    public String getName() {
        return "Hourly ($" + String.format("%.2f", ratePerHour) + "/hr)";
    }
}

class FlatRatePricing implements PricingStrategy { // implements = fulfills PricingStrategy contract
    private final double flatRate;    // final = immutable after construction

    public FlatRatePricing(double flatRate) {
        if (flatRate < 0) throw new IllegalArgumentException("Rate cannot be negative");
        this.flatRate = flatRate;
    }

    @Override
    public double calculateFee(Ticket ticket) {
        return flatRate;
    }

    @Override
    public String getName() {
        return "Flat Rate ($" + String.format("%.2f", flatRate) + ")";
    }
}

class VehicleTypePricing implements PricingStrategy { // implements = fulfills PricingStrategy contract
    private final double motorcycleRate; // final = rates are immutable after construction
    private final double carRate;        // final = immutable
    private final double truckRate;      // final = immutable

    public VehicleTypePricing(double motorcycleRate, double carRate, double truckRate) {
        if (motorcycleRate < 0 || carRate < 0 || truckRate < 0) {
            throw new IllegalArgumentException("Rates cannot be negative");
        }
        this.motorcycleRate = motorcycleRate;
        this.carRate = carRate;
        this.truckRate = truckRate;
    }

    @Override
    public double calculateFee(Ticket ticket) {
        long hours = ticket.getDurationInHours();
        switch (ticket.getVehicle().getType()) {
            case MOTORCYCLE: return hours * motorcycleRate;
            case CAR: return hours * carRate;
            case TRUCK: return hours * truckRate;
            default: return hours * carRate;
        }
    }

    @Override
    public String getName() {
        return "Vehicle-Type Based";
    }
}
