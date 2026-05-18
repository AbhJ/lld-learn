/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/Library.java — Library service with CAS-based concurrent borrow logic

package service;

import model.Book;
import model.Member;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public class Library {
    private final List<Book> books;   // final = reference won't change; books added at setup
    private final List<String> borrowLog; // synchronizedList = safe for concurrent appends from many threads

    public Library() {
        this.books = new ArrayList<>();
        this.borrowLog = Collections.synchronizedList(new ArrayList<>());
    }

    public void addBook(Book book) {
        books.add(book);
    }

    /**
     * Attempt to borrow a book. Uses CAS (AtomicInteger.decrementAndGet with rollback).
     * Returns true if the member successfully borrowed the book.
     */
    public boolean borrowBook(Member member, Book book) {
        boolean success = book.tryBorrow();
        if (success) {
            borrowLog.add(member.getName() + " borrowed \"" + book.getTitle() + "\"");
        }
        return success;
    }

    public void returnBook(Member member, Book book) {
        book.returnCopy();
        borrowLog.add(member.getName() + " returned \"" + book.getTitle() + "\"");
    }

    public List<String> getBorrowLog() { return borrowLog; }
}
