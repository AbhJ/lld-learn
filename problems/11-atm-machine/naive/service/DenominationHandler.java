/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/DenominationHandler.java — Chain of Responsibility base for cash denomination dispensing

import java.util.Map;

public abstract class DenominationHandler {
    protected DenominationHandler next;            // protected = next link in chain; subclasses traverse it

    /** Wire the next handler. Returns the argument so chains read left-to-right when fluent. */
    public DenominationHandler setNext(DenominationHandler next) {
        this.next = next;
        return next;
    }

    /** Denomination this handler is responsible for (e.g. 2000, 500, 200, ...). */
    public abstract int denomination();

    /**
     * Try to dispense as many notes of this denomination as possible, then forward
     * the remainder down the chain. Returns the amount that could not be served.
     *
     * @param remaining cents/dollars still owed
     * @param inventory live note counts (updated in place on dispense)
     * @param output    map of denomination -> notes dispensed (updated in place)
     */
    public int handle(int remaining, Map<Integer, Integer> inventory, Map<Integer, Integer> output) {
        if (remaining <= 0) return 0;
        int denom = denomination();
        int available = inventory.getOrDefault(denom, 0);
        int needed = remaining / denom;
        int toDispense = Math.min(needed, available);

        if (toDispense > 0) {
            output.put(denom, toDispense);
            remaining -= toDispense * denom;
            inventory.put(denom, available - toDispense);
        }

        if (remaining == 0) return 0;
        if (next != null) return next.handle(remaining, inventory, output);
        return remaining; // unsatisfied — caller must roll back
    }
}
