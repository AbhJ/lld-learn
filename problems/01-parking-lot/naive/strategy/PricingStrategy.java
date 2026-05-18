/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/PricingStrategy.java — Interchangeable pricing algorithms for parking fee calculation
// DESIGN PATTERN: Strategy

interface PricingStrategy {            // interface = contract; any pricing class MUST define these methods
    double calculateFee(Ticket ticket);
    String getName();
}

class HourlyPricing implements PricingStrategy { // implements = this class fulfills the interface contract
    private double ratePerHour;       // private = only this class uses the rate directly

    public HourlyPricing(double ratePerHour) {
        this.ratePerHour = ratePerHour;
    }

    @Override                           // tells compiler: I'm fulfilling the interface method
    public double calculateFee(Ticket ticket) {
        return ticket.getDurationInHours() * ratePerHour;
    }

    @Override                           // tells compiler: I'm fulfilling the interface method
    public String getName() {
        return "Hourly ($" + String.format("%.2f", ratePerHour) + "/hr)";
    }
}

class FlatRatePricing implements PricingStrategy { // implements = this class fulfills the interface contract
    private double flatRate;          // private = encapsulated; only calculateFee() uses it

    public FlatRatePricing(double flatRate) {
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
    private double motorcycleRate;    // private = internal rate; not exposed to callers
    private double carRate;           // private = internal rate
    private double truckRate;         // private = internal rate

    public VehicleTypePricing(double motorcycleRate, double carRate, double truckRate) {
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
