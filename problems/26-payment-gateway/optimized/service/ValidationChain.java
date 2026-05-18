/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ValidationChain.java — Parallel validation pipeline using CompletableFuture
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ValidationChain {
    public interface Validator {                  // interface = contract; validators can be added dynamically
        boolean validate(PaymentRequest request);
        String getName();
    }

    private List<Validator> validators;          // List = ordered collection; validators run in parallel via futures
    public ValidationChain() { this.validators = new ArrayList<>(); }
    public void addValidator(Validator v) { validators.add(v); }

    // WHY: Run all validators in parallel — total time = max(individual times) not sum
    public boolean validate(PaymentRequest request) {
        List<CompletableFuture<Boolean>> futures = new ArrayList<>(); // CompletableFuture = async result; runs validators in parallel threads
        for (Validator v : validators) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                boolean result = v.validate(request);
                System.out.println("  [parallel] " + v.getName() + ": " + (result ? "OK" : "FAILED"));
                return result;
            }));
        }
        // Wait for all and check if any failed
        return futures.stream().allMatch(CompletableFuture::join);
    }

    public static class AmountValidator implements Validator {
        @Override public boolean validate(PaymentRequest req) { return req.getAmount() > 0 && req.getAmount() <= 100000; }
        @Override public String getName() { return "Amount check"; }
    }

    public static class CardValidator implements Validator {
        @Override public boolean validate(PaymentRequest req) { return req.getCardNumber() != null && req.getCardNumber().length() >= 13; }
        @Override public String getName() { return "Card check"; }
    }
}
