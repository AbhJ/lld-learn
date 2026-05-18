/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ExpenseManager.java — Optimized: net balance approach minimizes transactions
import java.util.*;

public class ExpenseManager {
    private List<Expense> expenses;
    // Only store NET balance per user instead of all pairwise debts
    // This reduces O(n^2) pairwise tracking to O(n) net amounts
    private Map<String, Double> netBalances;              // HashMap = O(1) per-user net balance lookup

    public ExpenseManager() {
        this.expenses = new ArrayList<>();
        this.netBalances = new HashMap<>();
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
        User paidBy = expense.getPaidBy();
        for (Map.Entry<User, Double> entry : expense.getShares().entrySet()) {
            User owes = entry.getKey();
            double share = entry.getValue();
            if (!owes.equals(paidBy)) {
                // paidBy is owed money (positive), owes has debt (negative)
                netBalances.merge(paidBy.getId(), share, Double::sum);
                netBalances.merge(owes.getId(), -share, Double::sum);
            }
        }
    }

    public List<Balance> getBalances(Map<String, User> userMap) {
        // Derive pairwise from net for display purposes
        return calculateSettlementsAsBalances(userMap);
    }

    private List<Balance> calculateSettlementsAsBalances(Map<String, User> userMap) {
        List<Balance> result = new ArrayList<>();
        for (Settlement s : calculateSettlements(userMap)) {
            result.add(new Balance(s.getPayer(), s.getPayee(), s.getAmount()));
        }
        return result;
    }

    // WHY: Greedy algorithm on net amounts produces MINIMUM number of transactions
    // Sort creditors and debtors by amount, match largest with largest
    public List<Settlement> calculateSettlements(Map<String, User> userMap) {
        // Separate into debtors (negative net) and creditors (positive net)
        PriorityQueue<Map.Entry<String, Double>> debtors =   // PriorityQueue = always yields largest debtor in O(log n)
            new PriorityQueue<>((a, b) -> Double.compare(a.getValue(), b.getValue()));
        PriorityQueue<Map.Entry<String, Double>> creditors = // PriorityQueue = always yields largest creditor in O(log n)
            new PriorityQueue<>((a, b) -> Double.compare(b.getValue(), a.getValue()));

        for (Map.Entry<String, Double> entry : netBalances.entrySet()) {
            if (entry.getValue() < -0.01) {
                debtors.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
            } else if (entry.getValue() > 0.01) {
                creditors.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
            }
        }

        List<Settlement> settlements = new ArrayList<>();

        // WHY: matching largest debtor with largest creditor minimizes total transactions
        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            Map.Entry<String, Double> debtor = debtors.poll();
            Map.Entry<String, Double> creditor = creditors.poll();

            double debt = -debtor.getValue();
            double credit = creditor.getValue();
            double amount = Math.min(debt, credit);

            if (amount > 0.01) {
                settlements.add(new Settlement(
                    userMap.get(debtor.getKey()),
                    userMap.get(creditor.getKey()),
                    Math.round(amount * 100.0) / 100.0));
            }

            double remainingDebt = debt - amount;
            double remainingCredit = credit - amount;

            if (remainingDebt > 0.01) {
                debtors.add(new AbstractMap.SimpleEntry<>(debtor.getKey(), -remainingDebt));
            }
            if (remainingCredit > 0.01) {
                creditors.add(new AbstractMap.SimpleEntry<>(creditor.getKey(), remainingCredit));
            }
        }

        return settlements;
    }
}
