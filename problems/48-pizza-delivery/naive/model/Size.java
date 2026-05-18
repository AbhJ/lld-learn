/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Size.java — Enumerates pizza sizes
public enum Size { // enum = fixed pizza sizes with base prices; type-safe
    SMALL(8.99, "Small"), MEDIUM(11.99, "Medium"), LARGE(14.99, "Large"), EXTRA_LARGE(17.99, "Extra Large");
    private double basePrice; private String displayName; // private = price/name per size
    Size(double basePrice, String displayName) { this.basePrice = basePrice; this.displayName = displayName; }
    public double getBasePrice() { return basePrice; }
    public String getDisplayName() { return displayName; }
    @Override public String toString() { return displayName; }
}
