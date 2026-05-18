/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Crust.java — Enumerates crust types
public enum Crust { // enum = fixed set of crust types with associated costs
    THIN(0.00, "Thin"), REGULAR(0.00, "Regular"), THICK(1.50, "Thick"), STUFFED(3.00, "Stuffed");
    private double extraCost; private String displayName; // private = cost/name per crust type
    Crust(double extraCost, String displayName) { this.extraCost = extraCost; this.displayName = displayName; }
    public double getExtraCost() { return extraCost; }
    public String getDisplayName() { return displayName; }
}
