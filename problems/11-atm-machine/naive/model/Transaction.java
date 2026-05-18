/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Transaction.java — ATM transactions (withdrawal, deposit, transfer) with execution logic

import java.util.Map;

public abstract class Transaction { // abstract = can't create Transaction directly; must subclass
    protected double amount;         // protected = this class + subclasses can access
    protected Account sourceAccount; // protected = subclasses need this to execute transactions
    protected boolean successful;    // protected = subclasses set this after execution
    protected String failureReason;  // protected = subclasses set failure messages

    public Transaction(Account sourceAccount, double amount) {
        this.sourceAccount = sourceAccount;
        this.amount = amount;
        this.successful = false;
    }

    public abstract Receipt execute(ATM atm); // abstract = subclass MUST provide its own version

    public boolean isSuccessful() { return successful; }
    public String getFailureReason() { return failureReason; }
    public double getAmount() { return amount; }
}

class Withdrawal extends Transaction { // extends = inherits from Transaction; IS-A relationship
    public Withdrawal(Account account, double amount) {
        super(account, amount);
    }

    @Override                        // tells compiler: I'm replacing parent's method intentionally
    public Receipt execute(ATM atm) {
        if (amount > sourceAccount.getBalance()) {
            failureReason = "Insufficient balance";
            return null;
        }

        Map<Integer, Integer> dispensed = atm.getCashDispenser().dispense((int) amount);
        if (dispensed == null) {
            failureReason = "Cannot dispense exact amount with available denominations";
            return null;
        }

        sourceAccount.withdraw(amount);
        successful = true;

        StringBuilder details = new StringBuilder("Dispensed: ");
        for (Map.Entry<Integer, Integer> entry : dispensed.entrySet()) {
            details.append(entry.getValue()).append(" x $").append(entry.getKey()).append(", ");
        }

        return new Receipt("WITHDRAWAL", amount, sourceAccount.getBalance(),
                sourceAccount.getAccountId(), details.toString().replaceAll(", $", ""));
    }
}

class Deposit extends Transaction { // extends = inherits from Transaction; IS-A relationship
    public Deposit(Account account, double amount) {
        super(account, amount);
    }

    @Override                        // tells compiler: I'm replacing parent's method intentionally
    public Receipt execute(ATM atm) {
        if (amount <= 0) {
            failureReason = "Invalid deposit amount";
            return null;
        }
        sourceAccount.deposit(amount);
        successful = true;
        return new Receipt("DEPOSIT", amount, sourceAccount.getBalance(),
                sourceAccount.getAccountId(), "Cash deposit");
    }
}

class Transfer extends Transaction { // extends = inherits from Transaction; IS-A relationship
    private Account targetAccount;   // private = only Transfer can access the target

    public Transfer(Account sourceAccount, Account targetAccount, double amount) {
        super(sourceAccount, amount);
        this.targetAccount = targetAccount;
    }

    @Override                        // tells compiler: I'm replacing parent's method intentionally
    public Receipt execute(ATM atm) {
        if (amount > sourceAccount.getBalance()) {
            failureReason = "Insufficient balance";
            return null;
        }
        if (targetAccount == null) {
            failureReason = "Target account not found";
            return null;
        }

        boolean success = sourceAccount.transfer(targetAccount, amount);
        if (!success) {
            failureReason = "Transfer failed";
            return null;
        }

        successful = true;
        String details = String.format("Transfer to %s", targetAccount.getAccountId());
        return new Receipt("TRANSFER", amount, sourceAccount.getBalance(),
                sourceAccount.getAccountId(), details);
    }
}
