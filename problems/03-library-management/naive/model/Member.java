/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Member.java — Library member with borrowing limits and reservation tracking

import java.util.ArrayList;
import java.util.List;

class Member {
    private static int counter = 0;   // static = shared across all Members; generates unique IDs
    private String memberId;          // private = encapsulated identity
    private String name;              // private = accessed via getter
    private List<BorrowRecord> activeBorrows; // private = internal tracking of current loans
    private int maxBooks;             // private = borrowing limit hidden from callers

    public Member(String name) {
        this.memberId = "M-" + (++counter);
        this.name = name;
        this.activeBorrows = new ArrayList<>();
        this.maxBooks = 5;
    }

    public boolean canBorrow() {
        return activeBorrows.size() < maxBooks;
    }

    public void addBorrowRecord(BorrowRecord record) {
        activeBorrows.add(record);
    }

    public void removeBorrowRecord(BorrowRecord record) {
        activeBorrows.remove(record);
    }

    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public List<BorrowRecord> getActiveBorrows() { return activeBorrows; }

    @Override
    public String toString() {
        return name + " (" + memberId + ")";
    }

    public static void resetCounter() { counter = 0; }
}
