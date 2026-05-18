/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Settlement.java — A payment to settle outstanding debts
public class Settlement {
    private User payer;                                   // debtor in the minimized transaction set
    private User payee;                                   // creditor in the minimized transaction set
    private double amount;                                // amount from greedy creditor-debtor matching

    public Settlement(User payer, User payee, double amount) {
        this.payer = payer;
        this.payee = payee;
        this.amount = amount;
    }

    public User getPayer() { return payer; }
    public User getPayee() { return payee; }
    public double getAmount() { return amount; }

    @Override
    public String toString() {
        return String.format("%s -> %s: $%.2f", payer.getName(), payee.getName(), amount);
    }
}
