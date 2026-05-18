/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Expense.java — A shared expense with payer and calculated shares
import java.util.List;
import java.util.Map;

public class Expense {
    private String id;
    private String description;
    private double amount;
    private User paidBy;
    private Map<User, Double> shares;                     // HashMap = O(1) share lookup per user
    private SplitStrategy strategy;                       // Strategy pattern = interchangeable split algorithm

    public Expense(String id, String description, double amount, User paidBy,
                   List<User> participants, SplitStrategy strategy, Map<User, Double> params) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.paidBy = paidBy;
        this.strategy = strategy;
        this.shares = strategy.split(amount, participants, params);
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public User getPaidBy() { return paidBy; }
    public Map<User, Double> getShares() { return shares; }

    @Override
    public String toString() {
        return String.format("%s ($%.2f) paid by %s", description, amount, paidBy.getName());
    }
}
