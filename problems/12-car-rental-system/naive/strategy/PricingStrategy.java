/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/PricingStrategy.java — Interchangeable rental pricing algorithms (daily, weekly, weekend)
// DESIGN PATTERN: Strategy

public interface PricingStrategy { // interface = contract that pricing classes must fulfill
    double calculatePrice(Vehicle vehicle, int days);
    String getName();
}

class DailyPricing implements PricingStrategy { // implements = fulfills PricingStrategy contract
    @Override
    public double calculatePrice(Vehicle vehicle, int days) {
        return vehicle.getBaseDailyRate() * days;
    }

    @Override
    public String getName() { return "Daily"; }
}

class WeeklyPricing implements PricingStrategy { // implements = fulfills PricingStrategy contract
    private static final double WEEKLY_DISCOUNT = 0.85; // static final = compile-time constant

    @Override
    public double calculatePrice(Vehicle vehicle, int days) {
        int weeks = days / 7;
        int remainingDays = days % 7;
        double weeklyRate = vehicle.getBaseDailyRate() * 7 * WEEKLY_DISCOUNT;
        return (weeks * weeklyRate) + (remainingDays * vehicle.getBaseDailyRate());
    }

    @Override
    public String getName() { return "Weekly"; }
}

class WeekendPricing implements PricingStrategy { // implements = fulfills PricingStrategy contract
    private static final double WEEKEND_SURCHARGE = 1.20; // static final = compile-time constant

    @Override
    public double calculatePrice(Vehicle vehicle, int days) {
        return vehicle.getBaseDailyRate() * days * WEEKEND_SURCHARGE;
    }

    @Override
    public String getName() { return "Weekend"; }
}
