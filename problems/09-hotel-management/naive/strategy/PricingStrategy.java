/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/PricingStrategy.java — Swappable pricing algorithms (standard, seasonal, weekend)
// DESIGN PATTERN: Strategy

import java.time.DayOfWeek;
import java.time.LocalDate;

interface PricingStrategy {              // interface = contract; hotel can swap pricing algorithms
    double calculatePrice(Room room, LocalDate checkIn, int nights);
    String getName();
}

class StandardPricing implements PricingStrategy { // implements = fulfills PricingStrategy contract
    @Override
    public double calculatePrice(Room room, LocalDate checkIn, int nights) {
        return room.getBasePrice() * nights;
    }
    @Override
    public String getName() { return "Standard"; }
}

class SeasonalPricing implements PricingStrategy { // implements = fulfills PricingStrategy contract
    private double peakMultiplier;      // private = peak season markup encapsulated
    private int peakStartMonth;         // private = season start configured at construction
    private int peakEndMonth;           // private = season end configured at construction

    public SeasonalPricing(double peakMultiplier, int peakStartMonth, int peakEndMonth) {
        this.peakMultiplier = peakMultiplier;
        this.peakStartMonth = peakStartMonth;
        this.peakEndMonth = peakEndMonth;
    }

    @Override
    public double calculatePrice(Room room, LocalDate checkIn, int nights) {
        double total = 0;
        for (int i = 0; i < nights; i++) {
            LocalDate date = checkIn.plusDays(i);
            int month = date.getMonthValue();
            boolean isPeak = (peakStartMonth <= peakEndMonth)
                    ? (month >= peakStartMonth && month <= peakEndMonth)
                    : (month >= peakStartMonth || month <= peakEndMonth);
            total += room.getBasePrice() * (isPeak ? peakMultiplier : 1.0);
        }
        return total;
    }
    @Override
    public String getName() { return "Seasonal (x" + peakMultiplier + ")"; }
}

class WeekendPricing implements PricingStrategy { // implements = fulfills PricingStrategy contract
    private double weekendMultiplier;   // private = weekend markup factor encapsulated

    public WeekendPricing(double weekendMultiplier) {
        this.weekendMultiplier = weekendMultiplier;
    }

    @Override
    public double calculatePrice(Room room, LocalDate checkIn, int nights) {
        double total = 0;
        for (int i = 0; i < nights; i++) {
            LocalDate date = checkIn.plusDays(i);
            DayOfWeek day = date.getDayOfWeek();
            boolean isWeekend = (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY);
            total += room.getBasePrice() * (isWeekend ? weekendMultiplier : 1.0);
        }
        return total;
    }
    @Override
    public String getName() { return "Weekend (x" + weekendMultiplier + ")"; }
}
