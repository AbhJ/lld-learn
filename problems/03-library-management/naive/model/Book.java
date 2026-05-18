/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Book.java — Represents a book title with metadata, separate from physical copies

import java.util.ArrayList;
import java.util.List;

class Book {
    private String isbn;              // private = only this class manages ISBN
    private String title;             // private = encapsulated; accessed via getter
    private String author;            // private = encapsulated; accessed via getter
    private int publicationYear;      // private = hidden detail
    private List<BookCopy> copies;    // private = copies managed via addCopy()/getAvailableCopy()

    public Book(String isbn, String title, String author, int publicationYear) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
        this.copies = new ArrayList<>();
    }

    public BookCopy addCopy() {
        BookCopy copy = new BookCopy(this, copies.size() + 1);
        copies.add(copy);
        return copy;
    }

    public BookCopy getAvailableCopy() {
        for (BookCopy copy : copies) {
            if (copy.isAvailable()) {
                return copy;
            }
        }
        return null;
    }

    public int getAvailableCopyCount() {
        int count = 0;
        for (BookCopy copy : copies) {
            if (copy.isAvailable()) count++;
        }
        return count;
    }

    public int getTotalCopies() { return copies.size(); }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getPublicationYear() { return publicationYear; }
    public List<BookCopy> getCopies() { return copies; }

    @Override
    public String toString() {
        return "\"" + title + "\" by " + author + " (ISBN: " + isbn + ")";
    }
}
