/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Card.java — Bank card linking a card number to its associated account

public class Card {
    private String cardNumber;      // private = only this class can access; encapsulates data
    private String cardType;        // private = hidden from outside; must use getter to read
    private Account account;        // private = card owns reference to account; not exposed raw

    public Card(String cardNumber, String cardType, Account account) {
        this.cardNumber = cardNumber;
        this.cardType = cardType;
        this.account = account;
    }

    public String getCardNumber() { return cardNumber; }
    public String getCardType() { return cardType; }
    public Account getAccount() { return account; }

    public String getMaskedNumber() {
        if (cardNumber.length() <= 4) return cardNumber;
        return "****" + cardNumber.substring(cardNumber.length() - 4);
    }

    @Override                       // tells compiler: I'm replacing Object's method intentionally
    public String toString() {
        return String.format("%s ending %s", cardType, getMaskedNumber());
    }
}
