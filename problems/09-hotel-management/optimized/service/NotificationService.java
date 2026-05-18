/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/NotificationService.java — Concrete observer that logs booking events

class NotificationService implements BookingObserver { // implements = fulfills the BookingObserver contract
    private final String serviceName;                  // final = name set once, never changes

    public NotificationService(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override                                          // @Override = implementing interface methods
    public void onBookingConfirmed(Booking booking) {
        System.out.println("  [" + serviceName + "] Booked: " + booking);
    }

    @Override
    public void onCheckIn(Booking booking) {
        System.out.println("  [" + serviceName + "] CheckIn: " + booking.getGuest().getName());
    }

    @Override
    public void onCheckOut(Booking booking) {
        System.out.println("  [" + serviceName + "] CheckOut: " + booking.getGuest().getName());
    }
}
