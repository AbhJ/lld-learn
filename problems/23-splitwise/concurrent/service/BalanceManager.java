/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/BalanceManager.java — AtomicLong per user-pair balance with CAS updates

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class BalanceManager {
    // Key: "userA|userB" — alphabetically smaller user first
    // Value: positive = first owes second; negative = second owes first
    private final ConcurrentHashMap<String, AtomicLong> balances = new ConcurrentHashMap<>(); // ConcurrentHashMap + AtomicLong = lock-free per-pair updates

    private String pairKey(String u1, String u2) {
        return u1.compareTo(u2) < 0 ? u1 + "|" + u2 : u2 + "|" + u1;
    }

    /**
     * Records that 'debtor' owes 'creditor' the given amount (in cents).
     * Uses AtomicLong.addAndGet for lock-free update.
     */
    public void recordDebt(String debtor, String creditor, long amountCents) {
        if (debtor.equals(creditor)) return;
        String key = pairKey(debtor, creditor);
        AtomicLong balance = balances.computeIfAbsent(key, k -> new AtomicLong(0));

        // Convention: positive means first-alphabetical owes second-alphabetical
        if (debtor.compareTo(creditor) < 0) {
            balance.addAndGet(amountCents);
        } else {
            balance.addAndGet(-amountCents);
        }
    }

    public void addExpense(Expense expense) {
        String payer = expense.getPayer();
        long total = expense.getAmountCents();
        int numParticipants = expense.getParticipants().size();
        long share = total / numParticipants;

        for (String participant : expense.getParticipants()) {
            if (!participant.equals(payer)) {
                recordDebt(participant, payer, share);
            }
        }
    }

    /**
     * Returns net balance for a user (positive = others owe them, negative = they owe others).
     */
    public long getNetBalance(String user) {
        long net = 0;
        for (Map.Entry<String, AtomicLong> entry : balances.entrySet()) {
            String key = entry.getKey();
            String[] parts = key.split("\\|");
            long val = entry.getValue().get();
            if (parts[0].equals(user)) {
                net -= val; // user is first alphabetically, positive means they owe
            } else if (parts[1].equals(user)) {
                net += val; // user is second alphabetically, positive means first owes them
            }
        }
        return net;
    }

    /**
     * Sum of all net balances across all users should always be zero.
     */
    public long totalNetBalance(Set<String> users) {
        long total = 0;
        for (String user : users) {
            total += getNetBalance(user);
        }
        return total;
    }

    public Set<String> getPairKeys() {
        return balances.keySet();
    }
}
