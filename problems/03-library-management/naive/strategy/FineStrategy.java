/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/FineStrategy.java — Swappable fine calculation algorithms (daily, weekly)
// DESIGN PATTERN: Strategy

interface FineStrategy {              // interface = contract; any fine calculator MUST define these
    double calculateFine(int daysOverdue);
    String getName();
}

class DailyFine implements FineStrategy { // implements = fulfills the FineStrategy contract
    private double ratePerDay;        // private = rate only used internally

    public DailyFine(double ratePerDay) {
        this.ratePerDay = ratePerDay;
    }

    @Override
    public double calculateFine(int daysOverdue) {
        return daysOverdue * ratePerDay;
    }

    @Override
    public String getName() { return "Daily ($" + String.format("%.2f", ratePerDay) + "/day)"; }
}

class WeeklyFine implements FineStrategy { // implements = fulfills the FineStrategy contract
    private double ratePerWeek;       // private = rate only used internally

    public WeeklyFine(double ratePerWeek) {
        this.ratePerWeek = ratePerWeek;
    }

    @Override
    public double calculateFine(int daysOverdue) {
        int weeks = (daysOverdue + 6) / 7; // Round up to nearest week
        return weeks * ratePerWeek;
    }

    @Override
    public String getName() { return "Weekly ($" + String.format("%.2f", ratePerWeek) + "/week)"; }
}
