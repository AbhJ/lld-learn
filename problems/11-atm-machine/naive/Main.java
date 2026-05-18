/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the ATM system

/*
 * VARIATIONS FREQUENTLY ASKED:
 * 1. Multi-currency ATM - Support USD, EUR, GBP; exchange rate calculation
 * 2. Cardless withdrawal (UPI/OTP) - Phone-based authentication, OTP verification
 * 3. Check deposit - Image scanning, hold period, partial availability
 * 4. ATM network (interbank) - Cross-bank transactions, network fees, settlement
 * 5. Anti-fraud detection - Unusual amount, location mismatch, rapid successive withdrawals
 *
 * See VARIATIONS.md for full solution approaches.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== ATM Machine System Demo ===\n");

        // Setup
        ATM atm = new ATM();
        Account account1 = new Account("ACC-001", "Alice Johnson", 5000.00, "1234");
        Account account2 = new Account("ACC-002", "Bob Smith", 3000.00, "5678");
        atm.registerAccount(account1);
        atm.registerAccount(account2);

        Card card1 = new Card("4111111111114321", "VISA", account1);
        Card card2 = new Card("5222333344445678", "MasterCard", account2);

        // --- Test 1: Card Insert and Authentication ---
        System.out.println("--- Test 1: Card Insert and Authentication ---");
        atm.insertCard(card1);
        atm.authenticate("1234");
        System.out.println();

        // --- Test 2: Balance Inquiry ---
        System.out.println("--- Test 2: Balance Inquiry ---");
        atm.checkBalance();
        System.out.println();

        // --- Test 3: Cash Withdrawal ---
        System.out.println("--- Test 3: Cash Withdrawal ---");
        Receipt receipt = atm.withdraw(2750);
        if (receipt != null) {
            System.out.println(receipt);
        }
        System.out.println();

        // --- Test 4: Deposit ---
        System.out.println("--- Test 4: Deposit ---");
        receipt = atm.deposit(500);
        if (receipt != null) {
            System.out.println(receipt);
        }
        System.out.println();

        // Check balance after operations
        System.out.println("--- Balance After Withdrawal and Deposit ---");
        atm.checkBalance();
        System.out.println();

        // --- Test 5: Transfer ---
        System.out.println("--- Test 5: Transfer ---");
        receipt = atm.transfer("ACC-002", 1000);
        if (receipt != null) {
            System.out.println(receipt);
        }
        System.out.printf("Alice balance: $%.2f%n", account1.getBalance());
        System.out.printf("Bob balance: $%.2f%n", account2.getBalance());
        atm.ejectCard();
        System.out.println();

        // --- Test 6: Invalid PIN ---
        System.out.println("--- Test 6: Invalid PIN ---");
        atm.insertCard(card2);
        atm.authenticate("0000");
        atm.authenticate("1111");
        atm.authenticate("2222");
        System.out.println();

        // --- Test 7: Insufficient Balance ---
        System.out.println("--- Test 7: Insufficient Balance ---");
        atm.insertCard(card1);
        atm.authenticate("1234");
        atm.withdraw(999999);
        atm.ejectCard();
        System.out.println();

        // --- Test 8: Operations without Card ---
        System.out.println("--- Test 8: Operations Without Card ---");
        atm.checkBalance();
        atm.ejectCard();
        System.out.println();

        // --- Test 9: Transaction History ---
        System.out.println("--- Test 9: Transaction History ---");
        System.out.println("Total transactions: " + atm.getTransactionHistory().size());
        System.out.println();

        System.out.println("=== ATM Demo Complete ===");
    }
}
