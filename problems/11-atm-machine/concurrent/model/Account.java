/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Account.java — Account with AtomicLong balance for CAS-based withdrawal

import java.util.concurrent.atomic.AtomicLong;

class Account {
    private final String accountId;        // immutable identity — safe to read from any thread without sync
    private final String holderName;       // set once at creation — final guarantees visibility
    private final AtomicLong balanceCents; // multiple ATM threads withdraw/deposit — CAS loop prevents lost updates

    public Account(String accountId, String holderName, long balanceDollars) {
        this.accountId = accountId;
        this.holderName = holderName;
        this.balanceCents = new AtomicLong(balanceDollars * 100);
    }

    /**
     * CAS-based withdrawal — atomically checks and deducts balance.
     * Uses a compareAndSet loop: read balance, check sufficient, CAS to new balance.
     * Returns true if withdrawal succeeded, false if insufficient funds.
     */
    public boolean withdraw(long amountDollars) {
        long amountCents = amountDollars * 100;
        while (true) {
            long current = balanceCents.get();
            if (current < amountCents) {
                return false; // insufficient funds
            }
            long newBalance = current - amountCents;
            if (balanceCents.compareAndSet(current, newBalance)) {
                return true; // CAS succeeded
            }
            // CAS failed — another thread modified balance, retry
        }
    }

    /**
     * CAS-based deposit — atomically adds to balance.
     */
    public void deposit(long amountDollars) {
        balanceCents.addAndGet(amountDollars * 100);
    }

    public long getBalanceDollars() {
        return balanceCents.get() / 100;
    }

    public long getBalanceCents() {
        return balanceCents.get();
    }

    public String getAccountId() { return accountId; }
    public String getHolderName() { return holderName; }

    @Override
    public String toString() {
        return accountId + " (" + holderName + ") Balance: $" + getBalanceDollars();
    }
}
