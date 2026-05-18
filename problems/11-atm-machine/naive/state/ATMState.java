/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// state/ATMState.java — ATM state interface and implementations (NoCard, HasCard, Authenticated)
// DESIGN PATTERN: State

public interface ATMState { // interface = contract that all ATM states must fulfill
    void insertCard(ATM atm, Card card);
    void ejectCard(ATM atm);
    boolean authenticate(ATM atm, String pin);
    double checkBalance(ATM atm);
    Receipt performTransaction(ATM atm, Transaction transaction);
}

class NoCardState implements ATMState { // implements = fulfills the ATMState contract
    @Override
    public void insertCard(ATM atm, Card card) {
        atm.setCurrentCard(card);
        atm.setPinAttempts(0);
        atm.setState(new HasCardState());
        System.out.println("Card inserted: " + card);
    }

    @Override
    public void ejectCard(ATM atm) {
        System.out.println("No card to eject.");
    }

    @Override
    public boolean authenticate(ATM atm, String pin) {
        System.out.println("Please insert card first.");
        return false;
    }

    @Override
    public double checkBalance(ATM atm) {
        System.out.println("Please insert card first.");
        return -1;
    }

    @Override
    public Receipt performTransaction(ATM atm, Transaction transaction) {
        System.out.println("Please insert card first.");
        return null;
    }
}

class HasCardState implements ATMState { // implements = fulfills the ATMState contract
    private static final int MAX_PIN_ATTEMPTS = 3; // static final = constant shared by all instances

    @Override
    public void insertCard(ATM atm, Card card) {
        System.out.println("A card is already inserted.");
    }

    @Override
    public void ejectCard(ATM atm) {
        System.out.println("Card ejected.");
        atm.setCurrentCard(null);
        atm.setState(new NoCardState());
    }

    @Override
    public boolean authenticate(ATM atm, String pin) {
        Card card = atm.getCurrentCard();
        if (card.getAccount().validatePin(pin)) {
            atm.setState(new AuthenticatedState());
            System.out.println("PIN accepted. You are now authenticated.");
            return true;
        } else {
            atm.setPinAttempts(atm.getPinAttempts() + 1);
            int remaining = MAX_PIN_ATTEMPTS - atm.getPinAttempts();
            if (remaining <= 0) {
                System.out.println("Invalid PIN. Card retained. Please contact your bank.");
                atm.setCurrentCard(null);
                atm.setState(new NoCardState());
            } else {
                System.out.println("Invalid PIN. Attempts remaining: " + remaining);
            }
            return false;
        }
    }

    @Override
    public double checkBalance(ATM atm) {
        System.out.println("Please authenticate first.");
        return -1;
    }

    @Override
    public Receipt performTransaction(ATM atm, Transaction transaction) {
        System.out.println("Please authenticate first.");
        return null;
    }
}

class AuthenticatedState implements ATMState { // implements = fulfills the ATMState contract
    @Override
    public void insertCard(ATM atm, Card card) {
        System.out.println("A card is already inserted and authenticated.");
    }

    @Override
    public void ejectCard(ATM atm) {
        System.out.println("Card ejected. Thank you!");
        atm.setCurrentCard(null);
        atm.setState(new NoCardState());
    }

    @Override
    public boolean authenticate(ATM atm, String pin) {
        System.out.println("Already authenticated.");
        return true;
    }

    @Override
    public double checkBalance(ATM atm) {
        double balance = atm.getCurrentCard().getAccount().getBalance();
        System.out.printf("Account Balance: $%.2f%n", balance);
        return balance;
    }

    @Override
    public Receipt performTransaction(ATM atm, Transaction transaction) {
        Receipt receipt = transaction.execute(atm);
        if (transaction.isSuccessful()) {
            atm.addTransaction(transaction);
            System.out.println("Transaction successful. Receipt generated.");
        } else {
            System.out.println("Transaction failed: " + transaction.getFailureReason());
        }
        return receipt;
    }
}
