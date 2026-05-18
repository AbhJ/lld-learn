/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/BookingNotifier.java — Concrete observer that logs booking events

class BookingNotifier implements BookingObserver { // implements = fulfills the BookingObserver contract
    private final String notifierName;             // final = name set once, never changes

    public BookingNotifier(String notifierName) {
        this.notifierName = notifierName;
    }

    @Override                                      // @Override = implementing interface methods
    public void onBookingConfirmed(Booking booking) {
        System.out.println("  [" + notifierName + "] Booked: " + booking.getBookingId());
    }

    @Override
    public void onBookingCancelled(Booking booking) {
        System.out.println("  [" + notifierName + "] Cancelled: " + booking.getBookingId());
    }
}
