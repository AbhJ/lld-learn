/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Receipt.java — Transaction receipt with account info, amounts, and timestamps

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Receipt {
    private static int counter = 0;   // shared ID generator across all receipts
    private String receiptId;         // unique receipt identifier
    private String transactionType;   // WITHDRAWAL, DEPOSIT, TRANSFER
    private double amount;            // transaction amount
    private double balanceAfter;      // resulting account balance
    private String accountId;         // which account was used
    private LocalDateTime timestamp;  // when receipt was created
    private String details;           // extra info (dispensed notes, target account)

    public Receipt(String transactionType, double amount, double balanceAfter, String accountId, String details) {
        this.receiptId = "RCP-" + (++counter);
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.accountId = accountId;
        this.timestamp = LocalDateTime.now();
        this.details = details;
    }

    public String getReceiptId() { return receiptId; }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append("========== RECEIPT ==========\n");
        sb.append("Receipt #: ").append(receiptId).append("\n");
        sb.append("Date: ").append(timestamp.format(fmt)).append("\n");
        sb.append("Account: ").append(accountId).append("\n");
        sb.append("Type: ").append(transactionType).append("\n");
        sb.append(String.format("Amount: $%.2f\n", amount));
        sb.append(String.format("Balance: $%.2f\n", balanceAfter));
        if (details != null && !details.isEmpty()) {
            sb.append("Details: ").append(details).append("\n");
        }
        sb.append("=============================");
        return sb.toString();
    }
}
