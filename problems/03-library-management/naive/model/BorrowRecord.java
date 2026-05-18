/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/BorrowRecord.java — Tracks borrow lifecycle: checkout date, due date, return

import java.time.LocalDate;

class BorrowRecord {
    private BookCopy copy;            // private = encapsulated reference to borrowed copy
    private Member member;            // private = who borrowed it
    private LocalDate borrowDate;     // private = set at creation
    private LocalDate dueDate;        // private = computed from borrowDate + loanDays
    private LocalDate returnDate;     // private = null until markReturned() is called

    public BorrowRecord(BookCopy copy, Member member, LocalDate borrowDate, int loanDays) {
        this.copy = copy;
        this.member = member;
        this.borrowDate = borrowDate;
        this.dueDate = borrowDate.plusDays(loanDays);
        this.returnDate = null;
    }

    public void markReturned(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public boolean isOverdue(LocalDate today) {
        return returnDate == null && today.isAfter(dueDate);
    }

    public int getDaysOverdue(LocalDate returnDate) {
        if (!returnDate.isAfter(dueDate)) return 0;
        return (int) (returnDate.toEpochDay() - dueDate.toEpochDay());
    }

    public BookCopy getCopy() { return copy; }
    public Member getMember() { return member; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }

    @Override
    public String toString() {
        return member.getName() + " borrowed " + copy + " (due: " + dueDate + ")";
    }
}
