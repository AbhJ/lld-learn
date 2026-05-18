/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Card.java — Card linked to an Account

class Card {
    private final String cardNumber; // final = set once in constructor; safe publication to threads
    private final String pin;        // final = immutable after construction; thread-safe to read
    private final Account account;   // final = reference never changes; account itself is thread-safe

    public Card(String cardNumber, String pin, Account account) {
        this.cardNumber = cardNumber;
        this.pin = pin;
        this.account = account;
    }

    public boolean validatePin(String inputPin) {
        return pin.equals(inputPin);
    }

    public String getCardNumber() { return cardNumber; }
    public Account getAccount() { return account; }

    @Override
    public String toString() {
        return "Card ending " + cardNumber.substring(cardNumber.length() - 4);
    }
}
