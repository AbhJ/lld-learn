/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Balance.java — Pairwise debt between two users
public class Balance {
    private User from;                                    // derived from net-balance computation
    private User to;
    private double amount;                                // minimized via greedy matching algorithm

    public Balance(User from, User to, double amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    public User getFrom() { return from; }
    public User getTo() { return to; }
    public double getAmount() { return amount; }

    @Override
    public String toString() {
        return String.format("%s owes %s: $%.2f", from.getName(), to.getName(), amount);
    }
}
