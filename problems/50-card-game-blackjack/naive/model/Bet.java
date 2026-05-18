/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Bet.java — Represents a wager placed by a player
public class Bet {
    private double amount;   // private = wager amount set once at creation
    private boolean settled; // private = only settle() can mark bet as resolved

    public Bet(double amount) { this.amount = amount; this.settled = false; }
    public double getAmount() { return amount; }
    public double getWinAmount() { return amount; }
    public double getBlackjackWinAmount() { return amount * 1.5; }
    public void settle() { this.settled = true; }
}
