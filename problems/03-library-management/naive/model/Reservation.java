/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Reservation.java — Queue-based book reservation for unavailable titles

import java.time.LocalDate;

class Reservation {
    private Member member;            // private = who reserved it
    private Book book;                // private = what was reserved
    private LocalDate reservationDate; // private = when the reservation was made
    private boolean fulfilled;        // private = only fulfill() can change this

    public Reservation(Member member, Book book, LocalDate reservationDate) {
        this.member = member;
        this.book = book;
        this.reservationDate = reservationDate;
        this.fulfilled = false;
    }

    public void fulfill() { this.fulfilled = true; }

    public Member getMember() { return member; }
    public Book getBook() { return book; }
    public LocalDate getReservationDate() { return reservationDate; }
    public boolean isFulfilled() { return fulfilled; }

    @Override
    public String toString() {
        return member.getName() + " reserved \"" + book.getTitle() + "\" on " + reservationDate;
    }
}
