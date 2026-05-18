/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ExpenseManager.java — Naive: tracks all pairwise debts individually
import java.util.*;

public class ExpenseManager {
    private List<Expense> expenses;                       // private = full expense history
    // Naive: stores every pairwise balance — O(n^2) space for n users
    private Map<String, Map<String, Double>> balanceSheet; // private = nested map of who-owes-whom

    public ExpenseManager() {
        this.expenses = new ArrayList<>();
        this.balanceSheet = new HashMap<>();
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
        updateBalances(expense);
    }

    private void updateBalances(Expense expense) {
        User paidBy = expense.getPaidBy();
        for (Map.Entry<User, Double> entry : expense.getShares().entrySet()) {
            User owes = entry.getKey();
            double share = entry.getValue();
            if (!owes.equals(paidBy)) {
                addToBalance(owes, paidBy, share);
            }
        }
    }

    private void addToBalance(User from, User to, double amount) {
        String fromId = from.getId();
        String toId = to.getId();
        balanceSheet.computeIfAbsent(fromId, k -> new HashMap<>());
        balanceSheet.computeIfAbsent(toId, k -> new HashMap<>());

        double existing = balanceSheet.getOrDefault(toId, new HashMap<>()).getOrDefault(fromId, 0.0);
        if (existing > 0) {
            if (existing >= amount) {
                balanceSheet.get(toId).put(fromId, existing - amount);
                if (existing - amount < 0.01) balanceSheet.get(toId).remove(fromId);
            } else {
                balanceSheet.get(toId).remove(fromId);
                balanceSheet.get(fromId).put(toId, amount - existing);
            }
        } else {
            double current = balanceSheet.getOrDefault(fromId, new HashMap<>()).getOrDefault(toId, 0.0);
            balanceSheet.get(fromId).put(toId, current + amount);
        }
    }

    public List<Balance> getBalances(Map<String, User> userMap) {
        List<Balance> balances = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> outer : balanceSheet.entrySet()) {
            for (Map.Entry<String, Double> inner : outer.getValue().entrySet()) {
                if (inner.getValue() > 0.01) {
                    balances.add(new Balance(userMap.get(outer.getKey()), userMap.get(inner.getKey()), inner.getValue()));
                }
            }
        }
        return balances;
    }

    // Naive: settlement uses simple greedy — may not minimize transactions optimally
    public List<Settlement> calculateSettlements(Map<String, User> userMap) {
        Map<String, Double> netBalance = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> outer : balanceSheet.entrySet()) {
            for (Map.Entry<String, Double> inner : outer.getValue().entrySet()) {
                if (inner.getValue() > 0.01) {
                    netBalance.merge(outer.getKey(), -inner.getValue(), Double::sum);
                    netBalance.merge(inner.getKey(), inner.getValue(), Double::sum);
                }
            }
        }

        List<Map.Entry<String, Double>> debtors = new ArrayList<>();
        List<Map.Entry<String, Double>> creditors = new ArrayList<>();
        for (Map.Entry<String, Double> entry : netBalance.entrySet()) {
            if (entry.getValue() < -0.01) debtors.add(entry);
            else if (entry.getValue() > 0.01) creditors.add(entry);
        }

        List<Settlement> settlements = new ArrayList<>();
        int i = 0, j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            double debt = -debtors.get(i).getValue();
            double credit = creditors.get(j).getValue();
            double amount = Math.min(debt, credit);
            if (amount > 0.01) {
                settlements.add(new Settlement(
                    userMap.get(debtors.get(i).getKey()),
                    userMap.get(creditors.get(j).getKey()),
                    Math.round(amount * 100.0) / 100.0));
            }
            debtors.get(i).setValue(debtors.get(i).getValue() + amount);
            creditors.get(j).setValue(creditors.get(j).getValue() - amount);
            if (Math.abs(debtors.get(i).getValue()) < 0.01) i++;
            if (Math.abs(creditors.get(j).getValue()) < 0.01) j++;
        }
        return settlements;
    }
}
