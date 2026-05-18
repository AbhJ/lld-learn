/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/CashDispenser.java — TreeMap-backed inventory; dispense delegates to a Chain of Responsibility

import java.util.*;

public class CashDispenser {
    private TreeMap<Integer, Integer> denominations; // TreeMap = auto-sorted keys; O(log n) access
    private DenominationHandler chainHead;           // private = head of CoR chain (largest denom first)

    public CashDispenser() {
        // TreeMap keeps denominations sorted; we use descendingMap() for largest-first
        denominations = new TreeMap<>(Collections.reverseOrder()); // reverseOrder = largest denom first
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
        denominations.merge(denomination, count, Integer::sum);
    }

    /**
     * Greedy dispensing through a Chain of Responsibility. Each handler tries its
     * own denomination first and forwards the remainder to the next. The TreeMap
     * is still used so handlers see notes in O(log n) and so removals stay sorted.
     */
    public Map<Integer, Integer> dispense(int amount) {
        if (amount <= 0) return null;

        // Snapshot inventory so we can roll back if the chain can't satisfy the amount.
        TreeMap<Integer, Integer> snapshot = new TreeMap<>(denominations);
        Map<Integer, Integer> output = new LinkedHashMap<>(); // LinkedHashMap = preserves insertion order

        int unsatisfied = chainHead.handle(amount, denominations, output);

        if (unsatisfied != 0) {
            denominations.clear();
            denominations.putAll(snapshot);
            return null;
        }

        // Cleanup: drop denominations whose count fell to zero so iteration stays compact.
        denominations.values().removeIf(count -> count == 0);
        return output;
    }

    /**
     * O(1) check if a denomination exists, O(log d) retrieval from TreeMap.
     */
    public boolean hasDenomination(int denomination) {
        Integer count = denominations.get(denomination);
        return count != null && count > 0;
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
        for (Map.Entry<Integer, Integer> entry : denominations.entrySet()) {
            sb.append(String.format("  $%d: %d notes\n", entry.getKey(), entry.getValue()));
        }
        sb.append(String.format("  Total: $%d", getTotalCash()));
        return sb.toString();
    }
}
