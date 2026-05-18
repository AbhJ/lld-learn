/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/SplitwiseService.java — Facade for expense tracking operations
import java.util.*;

public class SplitwiseService {                           // Facade pattern = single entry point for expense ops
    private Map<String, User> users;                      // private = internal user registry
    private Map<String, Group> groups;                    // private = internal group registry
    private ExpenseManager expenseManager;                // private = delegates balance calculations

    public SplitwiseService() {
        this.users = new HashMap<>();
        this.groups = new HashMap<>();
        this.expenseManager = new ExpenseManager();
    }

    public User addUser(String id, String name, String email) {
        User user = new User(id, name, email);
        users.put(id, user);
        return user;
    }

    public Group createGroup(String id, String name, List<User> members) {
        Group group = new Group(id, name);
        for (User member : members) group.addMember(member);
        groups.put(id, group);
        return group;
    }

    public Expense addExpense(String id, String description, double amount, User paidBy,
                              List<User> participants, SplitStrategy strategy, Map<User, Double> params) {
        Expense expense = new Expense(id, description, amount, paidBy, participants, strategy, params);
        expenseManager.addExpense(expense);
        return expense;
    }

    public Expense addGroupExpense(String groupId, String expenseId, String description,
                                    double amount, User paidBy, SplitStrategy strategy, Map<User, Double> params) {
        Group group = groups.get(groupId);
        if (group == null) throw new IllegalArgumentException("Group not found: " + groupId);
        Expense expense = new Expense(expenseId, description, amount, paidBy, group.getMembers(), strategy, params);
        group.addExpense(expense);
        expenseManager.addExpense(expense);
        return expense;
    }

    public List<Balance> getBalances() { return expenseManager.getBalances(users); }
    public List<Settlement> getSettlements() { return expenseManager.calculateSettlements(users); }
    public Map<String, User> getUsers() { return users; }
}
