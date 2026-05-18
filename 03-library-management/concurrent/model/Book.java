/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Book.java — Book with AtomicInteger for thread-safe available copy tracking

package model;

import java.util.concurrent.atomic.AtomicInteger;

public class Book {
    private final String isbn;        // final = immutable identity; safe publication to all threads
    private final String title;       // final = set once; no synchronization needed
    private final String author;      // final = set once; no synchronization needed
    private final AtomicInteger availableCopies; // AtomicInteger = CAS-based decrement prevents over-borrowing
    private final int totalCopies;    // final = never changes; reference value for capacity

    public Book(String isbn, String title, String author, int totalCopies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.totalCopies = totalCopies;
        this.availableCopies = new AtomicInteger(totalCopies);
    }

    /**
     * CAS-based borrow: decrementAndGet, if result < 0, rollback.
     * Returns true if borrow succeeded.
     */
    public boolean tryBorrow() {
        int remaining = availableCopies.decrementAndGet();
        if (remaining < 0) {
            // Rollback — no copies were actually available
            availableCopies.incrementAndGet();
            return false;
        }
        return true;
    }

    public void returnCopy() {
        availableCopies.incrementAndGet();
    }

    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getAvailableCopies() { return availableCopies.get(); }
    public int getTotalCopies() { return totalCopies; }

    @Override
    public String toString() {
        return "Book(\"" + title + "\" available=" + availableCopies.get() + "/" + totalCopies + ")";
    }
}
