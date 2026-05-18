/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Library.java — Facade coordinating books, members, borrowing, and reservations

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

class Library {
    private String name;              // private = library identity
    private List<Book> books;         // private = internal catalog
    private List<Member> members;     // private = internal member registry
    private List<BorrowRecord> allRecords; // private = borrowing history
    private Queue<Reservation> reservations; // private = FIFO queue for fair reservation order
    private FineStrategy fineStrategy; // private = swappable fine algorithm (Strategy pattern)
    private SearchStrategy searchStrategy; // private = swappable search algorithm (Strategy pattern)
    private int loanDays;             // private = configurable loan period

    public Library(String name) {
        this.name = name;
        this.books = new ArrayList<>();
        this.members = new ArrayList<>();
        this.allRecords = new ArrayList<>();
        this.reservations = new LinkedList<>();
        this.fineStrategy = new DailyFine(1.0);
        this.searchStrategy = new TitleSearch();
        this.loanDays = 14;
    }

    public void setFineStrategy(FineStrategy strategy) { this.fineStrategy = strategy; }
    public void setSearchStrategy(SearchStrategy strategy) { this.searchStrategy = strategy; }

    public Book addBook(String isbn, String title, String author, int year, int copies) {
        Book book = new Book(isbn, title, author, year);
        for (int i = 0; i < copies; i++) {
            book.addCopy();
        }
        books.add(book);
        return book;
    }

    public Member registerMember(String name) {
        Member member = new Member(name);
        members.add(member);
        return member;
    }

    public BorrowRecord borrowBook(Member member, Book book, LocalDate date) {
        if (!member.canBorrow()) {
            System.out.println("  " + member.getName() + " has reached borrowing limit.");
            return null;
        }
        BookCopy copy = book.getAvailableCopy();
        if (copy == null) {
            System.out.println("  No copies available for \"" + book.getTitle() + "\"");
            return null;
        }
        copy.checkout();
        BorrowRecord record = new BorrowRecord(copy, member, date, loanDays);
        member.addBorrowRecord(record);
        allRecords.add(record);
        return record;
    }

    public double returnBook(BorrowRecord record, LocalDate returnDate) {
        record.getCopy().returnCopy();
        record.markReturned(returnDate);
        record.getMember().removeBorrowRecord(record);

        double fine = 0;
        int daysOverdue = record.getDaysOverdue(returnDate);
        if (daysOverdue > 0) {
            fine = fineStrategy.calculateFine(daysOverdue);
        }

        // Check reservations for this book
        checkReservations(record.getCopy().getBook());

        return fine;
    }

    public Reservation reserveBook(Member member, Book book, LocalDate date) {
        Reservation reservation = new Reservation(member, book, date);
        reservations.add(reservation);
        return reservation;
    }

    private void checkReservations(Book book) {
        for (Reservation res : reservations) {
            if (!res.isFulfilled() && res.getBook().equals(book)) {
                if (book.getAvailableCopyCount() > 0) {
                    res.fulfill();
                    System.out.println("  [Notification] " + res.getMember().getName() +
                            ": \"" + book.getTitle() + "\" is now available!");
                }
                break;
            }
        }
    }

    public List<Book> search(String query) {
        return searchStrategy.search(books, query);
    }

    public String getName() { return name; }
    public List<Book> getBooks() { return books; }
    public List<Member> getMembers() { return members; }
    public FineStrategy getFineStrategy() { return fineStrategy; }
}
