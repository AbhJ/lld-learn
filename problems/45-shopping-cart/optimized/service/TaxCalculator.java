/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/TaxCalculator.java — Computes applicable taxes based on location
import java.util.HashMap;
import java.util.Map;

public class TaxCalculator {
    private Map<String, Double> taxRates;

    public TaxCalculator() {
        taxRates = new HashMap<>();
        taxRates.put("CA", 9.5); taxRates.put("NY", 8.875); taxRates.put("TX", 8.25);
    }

    public double calculateTax(double amount, String region) {
        double rate = taxRates.getOrDefault(region, 0.0);
        return amount * (rate / 100.0);
    }
}
