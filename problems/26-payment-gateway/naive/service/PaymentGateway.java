/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/PaymentGateway.java — Sequential validation and processing
import java.util.*;

public class PaymentGateway {
    private Map<String, PaymentProcessor> processors;    // private = internal registry hidden from callers
    private ValidationChain validationChain;
    private Map<String, Transaction> transactions;
    private Set<String> processedKeys;                   // Set = no duplicates; used for idempotency
    private int txnCounter = 0;

    public PaymentGateway() {
        this.processors = new HashMap<>();
        this.validationChain = new ValidationChain();
        this.transactions = new LinkedHashMap<>();
        this.processedKeys = new HashSet<>();
        validationChain.addValidator(new ValidationChain.AmountValidator());
        validationChain.addValidator(new ValidationChain.CardValidator());
    }

    public void registerProcessor(String type, PaymentProcessor processor) {
        processors.put(type, processor);
    }

    // Naive: sequential validation, HashSet for idempotency (not concurrent-safe)
    public Transaction processPayment(PaymentRequest request) {
        if (processedKeys.contains(request.getIdempotencyKey())) {
            System.out.println("  Duplicate request (idempotency key: " + request.getIdempotencyKey() + ")");
            return null;
        }
        if (!validationChain.validate(request)) return null;

        PaymentProcessor processor = processors.get(request.getProcessorType());
        if (processor == null) { System.out.println("  No processor for: " + request.getProcessorType()); return null; }

        String txnId = "TXN-" + String.format("%03d", ++txnCounter);
        Transaction txn = new Transaction(txnId, request.getAmount(), request.getCurrency(), request.getProcessorType(), request.getDescription());

        boolean success = processor.processPayment(request);
        txn.setStatus(success ? TransactionStatus.SUCCESS : TransactionStatus.FAILED);
        transactions.put(txnId, txn);
        processedKeys.add(request.getIdempotencyKey());
        System.out.println("  " + txn);
        return txn;
    }

    public Collection<Transaction> getAllTransactions() { return transactions.values(); }
}
