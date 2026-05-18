/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Coin.java — Enum defining coin denominations with cent values

enum Coin {                           // enum = fixed set of coin types with associated values
    PENNY(1),
    NICKEL(5),
    DIME(10),
    QUARTER(25);

    private int valueInCents;         // private = each enum constant stores its cent value

    Coin(int valueInCents) {
        this.valueInCents = valueInCents;
    }

    public int getValue() { return valueInCents; }

    public static String formatCents(int cents) {
        return "$" + String.format("%.2f", cents / 100.0);
    }
}
