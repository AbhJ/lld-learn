/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ValidationChain.java — Sequential validation pipeline
import java.util.ArrayList;
import java.util.List;

public class ValidationChain {
    public interface Validator {              // interface = contract; any validator must implement these
        boolean validate(PaymentRequest request);
        String getName();
    }

    private List<Validator> validators;
    public ValidationChain() { this.validators = new ArrayList<>(); }
    public void addValidator(Validator v) { validators.add(v); }

    // Naive: validators run sequentially, one after another
    public boolean validate(PaymentRequest request) {
        for (Validator v : validators) {
            System.out.println("  Validating: " + v.getName() + "...");
            if (!v.validate(request)) {
                System.out.println("  FAILED: " + v.getName());
                return false;
            }
        }
        return true;
    }

    public static class AmountValidator implements Validator { // static = no reference to outer class; implements = fulfills Validator contract
        @Override public boolean validate(PaymentRequest req) { return req.getAmount() > 0 && req.getAmount() <= 100000; } // @Override = must match interface method
        @Override public String getName() { return "Amount check"; }
    }

    public static class CardValidator implements Validator { // static inner class = standalone; implements = fulfills Validator contract
        @Override public boolean validate(PaymentRequest req) { return req.getCardNumber() != null && req.getCardNumber().length() >= 13; }
        @Override public String getName() { return "Card check"; }
    }
}
