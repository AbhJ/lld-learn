/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/CashDispenser.java — Cash inventory; dispense delegates to a Chain of Responsibility of DenominationHandlers

import java.util.*;

public class CashDispenser {
    private Map<Integer, Integer> denominations; // private = hides internal cash inventory
    private DenominationHandler chainHead;       // private = head of CoR chain (largest denom first)

    public CashDispenser() {
        denominations = new LinkedHashMap<>();
        // Default denominations
        denominations.put(2000, 50);
        denominations.put(500, 100);
        denominations.put(200, 100);
        denominations.put(100, 200);
        denominations.put(50, 200);
        denominations.put(20, 300);

        // Build the CoR chain: largest denom first, each forwards remainder to the next.
        // 2000 -> 500 -> 200 -> 100 -> 50 -> 20
        chainHead = new TwoThousandHandler();
        chainHead.setNext(new FiveHundredHandler())
                 .setNext(new TwoHundredHandler())
                 .setNext(new HundredHandler())
                 .setNext(new FiftyHandler())
                 .setNext(new TwentyHandler());
    }

    public void loadDenomination(int denomination, int count) {
        denominations.put(denomination, denominations.getOrDefault(denomination, 0) + count);
    }

    public Map<Integer, Integer> dispense(int amount) {
        if (amount <= 0) return null;

        // Snapshot inventory so we can roll back if the chain can't satisfy the amount.
        Map<Integer, Integer> snapshot = new LinkedHashMap<>(denominations);
        Map<Integer, Integer> output = new LinkedHashMap<>();

        int unsatisfied = chainHead.handle(amount, denominations, output);

        if (unsatisfied != 0) {
            // Roll back: restore the snapshot, return null to signal failure.
            denominations.clear();
            denominations.putAll(snapshot);
            return null;
        }

        return output;
    }

    public int getTotalCash() {
        int total = 0;
        for (Map.Entry<Integer, Integer> entry : denominations.entrySet()) {
            total += entry.getKey() * entry.getValue();
        }
        return total;
    }

    public String getStatus() {
        StringBuilder sb = new StringBuilder("Cash Dispenser Status:\n");
        List<Integer> sortedDenoms = new ArrayList<>(denominations.keySet());
        sortedDenoms.sort(Collections.reverseOrder());
        for (int denom : sortedDenoms) {
            sb.append(String.format("  $%d: %d notes\n", denom, denominations.get(denom)));
        }
        sb.append(String.format("  Total: $%d", getTotalCash()));
        return sb.toString();
    }
}
