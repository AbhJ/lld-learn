/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Account.java — Bank account with balance, PIN validation, and transaction operations

public class Account {
    private String accountId;       // private = only this class can access; encapsulates data
    private String holderName;      // private = hidden from outside; must use getter to read
    private double balance;         // private = prevents outsiders from changing balance directly
    private String pin;             // private = sensitive data; never exposed outside this class

    public Account(String accountId, String holderName, double balance, String pin) {
        this.accountId = accountId;
        this.holderName = holderName;
        this.balance = balance;
        this.pin = pin;
    }

    public String getAccountId() { return accountId; }
    public String getHolderName() { return holderName; }
    public double getBalance() { return balance; }

    public boolean validatePin(String inputPin) {
        return this.pin.equals(inputPin);
    }

    public synchronized boolean withdraw(double amount) { // synchronized = one thread at a time
        if (amount <= 0 || amount > balance) return false;
        balance -= amount;
        return true;
    }

    public synchronized void deposit(double amount) { // synchronized = prevents race conditions
        if (amount > 0) balance += amount;
    }

    public synchronized boolean transfer(Account target, double amount) { // synchronized = atomic
        if (amount <= 0 || amount > balance) return false;
        balance -= amount;
        target.deposit(amount);
        return true;
    }

    @Override                       // tells compiler: I'm replacing Object's method intentionally
    public String toString() {
        return String.format("Account[%s, %s, $%.2f]", accountId, holderName, balance);
    }
}
