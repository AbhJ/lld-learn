/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Group.java — A group of users sharing expenses
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Group {
    private String id;
    private String name;
    private List<User> members;                           // private = only Group manages its member list
    private List<Expense> expenses;                       // private = group's expense history

    public Group(String id, String name) {
        this.id = id;
        this.name = name;
        this.members = new ArrayList<>();
        this.expenses = new ArrayList<>();
    }

    public void addMember(User user) {
        if (!members.contains(user)) members.add(user);
    }

    public void addExpense(Expense expense) { expenses.add(expense); }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<User> getMembers() { return Collections.unmodifiableList(members); }
    public List<Expense> getExpenses() { return Collections.unmodifiableList(expenses); }

    @Override
    public String toString() { return name + " (" + members.size() + " members)"; }
}
