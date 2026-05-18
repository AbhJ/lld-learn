/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ATM.java — ATM context class managing hardware, current card, and delegating to states

import java.util.*;

public class ATM {
    private ATMState currentState;              // private = state pattern; only ATM manages state
    private Card currentCard;                   // private = currently inserted card; encapsulated
    private CashDispenser cashDispenser;        // private = ATM owns the dispenser hardware
    private int pinAttempts;                    // private = tracks failed PIN attempts internally
    private List<Transaction> transactionHistory; // private = audit log hidden from outside
    private Map<String, Account> accounts;      // private = registered accounts; internal registry

    public ATM() {
        this.currentState = new NoCardState();
        this.cashDispenser = new CashDispenser();
        this.pinAttempts = 0;
        this.transactionHistory = new ArrayList<>();
        this.accounts = new HashMap<>();
    }

    public void registerAccount(Account account) {
        accounts.put(account.getAccountId(), account);
    }

    public Account findAccount(String accountId) {
        return accounts.get(accountId);
    }

    // Delegate methods to current state
    public void insertCard(Card card) {
        currentState.insertCard(this, card);
    }

    public void ejectCard() {
        currentState.ejectCard(this);
    }

    public boolean authenticate(String pin) {
        return currentState.authenticate(this, pin);
    }

    public double checkBalance() {
        return currentState.checkBalance(this);
    }

    public Receipt withdraw(double amount) {
        Withdrawal withdrawal = new Withdrawal(currentCard.getAccount(), amount);
        return currentState.performTransaction(this, withdrawal);
    }

    public Receipt deposit(double amount) {
        Deposit dep = new Deposit(currentCard.getAccount(), amount);
        return currentState.performTransaction(this, dep);
    }

    public Receipt transfer(String targetAccountId, double amount) {
        Account target = accounts.get(targetAccountId);
        Transfer txn = new Transfer(currentCard.getAccount(), target, amount);
        return currentState.performTransaction(this, txn);
    }

    // State management
    public void setState(ATMState state) { this.currentState = state; }
    public ATMState getState() { return currentState; }

    public Card getCurrentCard() { return currentCard; }
    public void setCurrentCard(Card card) { this.currentCard = card; }

    public CashDispenser getCashDispenser() { return cashDispenser; }

    public int getPinAttempts() { return pinAttempts; }
    public void setPinAttempts(int attempts) { this.pinAttempts = attempts; }

    public void addTransaction(Transaction txn) { transactionHistory.add(txn); }
    public List<Transaction> getTransactionHistory() { return Collections.unmodifiableList(transactionHistory); }
}
