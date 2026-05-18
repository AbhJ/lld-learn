/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/PaymentRequest.java — Payment initiation data
public class PaymentRequest {
    private String idempotencyKey;
    private double amount;
    private String currency;
    private String cardNumber;
    private String processorType;
    private String description;

    public PaymentRequest(String idempotencyKey, double amount, String currency,
                          String cardNumber, String processorType, String description) {
        this.idempotencyKey = idempotencyKey; this.amount = amount; this.currency = currency;
        this.cardNumber = cardNumber; this.processorType = processorType; this.description = description;
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public double getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getCardNumber() { return cardNumber; }
    public String getProcessorType() { return processorType; }
    public String getDescription() { return description; }
}
