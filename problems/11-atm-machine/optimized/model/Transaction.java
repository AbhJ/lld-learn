/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Transaction.java — ATM transactions (withdrawal, deposit, transfer) with execution logic

import java.util.Map;

public abstract class Transaction { // abstract = can't instantiate directly; must subclass
    protected double amount;         // protected = subclasses access for execution
    protected Account sourceAccount; // protected = subclasses need for balance ops
    protected boolean successful;    // protected = subclasses set after execution
    protected String failureReason;  // protected = subclasses set on failure

    public Transaction(Account sourceAccount, double amount) {
        this.sourceAccount = sourceAccount;
        this.amount = amount;
        this.successful = false;
    }

    public abstract Receipt execute(ATM atm);

    public boolean isSuccessful() { return successful; }
    public String getFailureReason() { return failureReason; }
    public double getAmount() { return amount; }
}

class Withdrawal extends Transaction {
    public Withdrawal(Account account, double amount) {
        super(account, amount);
    }

    @Override
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

class Deposit extends Transaction {
    public Deposit(Account account, double amount) {
        super(account, amount);
    }

    @Override
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

class Transfer extends Transaction {
    private Account targetAccount;

    public Transfer(Account sourceAccount, Account targetAccount, double amount) {
        super(sourceAccount, amount);
        this.targetAccount = targetAccount;
    }

    @Override
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
