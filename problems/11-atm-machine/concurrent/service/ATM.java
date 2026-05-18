/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/ATM.java — ATM service using CAS-based Account for thread-safe withdrawals

import java.util.concurrent.atomic.AtomicInteger;

class ATM {
    private final String atmId;      // final = immutable identity; safe to read from any thread
    private final AtomicInteger successfulTransactions = new AtomicInteger(0); // AtomicInteger = lock-free thread-safe counter
    private final AtomicInteger failedTransactions = new AtomicInteger(0);     // AtomicInteger = CAS-based increment, no locks

    public ATM(String atmId) {
        this.atmId = atmId;
    }

    /**
     * Attempt withdrawal from account. Thread-safe via Account's CAS-based withdraw.
     * Multiple concurrent calls will not allow overdraft.
     */
    public boolean withdraw(Account account, long amount) {
        boolean success = account.withdraw(amount);
        if (success) {
            successfulTransactions.incrementAndGet();
        } else {
            failedTransactions.incrementAndGet();
        }
        return success;
    }

    public boolean deposit(Account account, long amount) {
        account.deposit(amount);
        successfulTransactions.incrementAndGet();
        return true;
    }

    public long checkBalance(Account account) {
        return account.getBalanceDollars();
    }

    public int getSuccessfulTransactions() { return successfulTransactions.get(); }
    public int getFailedTransactions() { return failedTransactions.get(); }
    public String getAtmId() { return atmId; }
}
