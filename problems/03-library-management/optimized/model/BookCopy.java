/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/BookCopy.java — Physical copy of a book with availability tracking

class BookCopy {
    private Book book;                // private = links copy to its Book; hidden from outside
    private int copyNumber;           // private = internal identity
    private boolean available;        // private = only checkout()/returnCopy() modify this

    public BookCopy(Book book, int copyNumber) {
        this.book = book;
        this.copyNumber = copyNumber;
        this.available = true;
    }

    public void checkout() { this.available = false; }
    public void returnCopy() { this.available = true; }

    public boolean isAvailable() { return available; }
    public Book getBook() { return book; }
    public int getCopyNumber() { return copyNumber; }

    @Override
    public String toString() {
        return book.getTitle() + " (Copy #" + copyNumber + ")";
    }
}
