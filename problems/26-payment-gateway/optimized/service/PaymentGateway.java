/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/PaymentGateway.java — Thread-safe gateway with ConcurrentHashMap idempotency
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentGateway {
    private Map<String, PaymentProcessor> processors;
    private ValidationChain validationChain;
    private Map<String, Transaction> transactions;       // synchronizedMap = thread-safe wrapper around LinkedHashMap
    private ConcurrentHashMap<String, Boolean> processedKeys; // ConcurrentHashMap = lock-free reads; putIfAbsent is atomic
    private int txnCounter = 0;

    public PaymentGateway() {
        this.processors = new HashMap<>();
        this.validationChain = new ValidationChain();
        this.transactions = Collections.synchronizedMap(new LinkedHashMap<>());
        this.processedKeys = new ConcurrentHashMap<>();
        validationChain.addValidator(new ValidationChain.AmountValidator());
        validationChain.addValidator(new ValidationChain.CardValidator());
    }

    public void registerProcessor(String type, PaymentProcessor processor) {
        processors.put(type, processor);
    }

    public Transaction processPayment(PaymentRequest request) {
        // WHY: putIfAbsent is atomic — no race condition between check and insert
        if (processedKeys.putIfAbsent(request.getIdempotencyKey(), Boolean.TRUE) != null) {
            System.out.println("  Duplicate request blocked atomically (key: " + request.getIdempotencyKey() + ")");
            return null;
        }

        // Parallel validation
        if (!validationChain.validate(request)) {
            processedKeys.remove(request.getIdempotencyKey()); // allow retry on validation failure
            return null;
        }

        PaymentProcessor processor = processors.get(request.getProcessorType());
        if (processor == null) { System.out.println("  No processor for: " + request.getProcessorType()); return null; }

        String txnId;
        synchronized (this) { txnId = "TXN-" + String.format("%03d", ++txnCounter); } // synchronized = only one thread increments counter at a time
        Transaction txn = new Transaction(txnId, request.getAmount(), request.getCurrency(), request.getProcessorType(), request.getDescription());

        boolean success = processor.processPayment(request);
        txn.setStatus(success ? TransactionStatus.SUCCESS : TransactionStatus.FAILED);
        transactions.put(txnId, txn);
        System.out.println("  " + txn);
        return txn;
    }

    public Collection<Transaction> getAllTransactions() { return transactions.values(); }
}
