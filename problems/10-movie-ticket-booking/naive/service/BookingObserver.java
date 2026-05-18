/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/BookingObserver.java — Observer pattern interface for booking events
// DESIGN PATTERN: Observer
//
// WHO IMPLEMENTS THIS? → BookingNotifier (in BookingNotifier.java)
// WHO CALLS IT? → BookingSystem calls observers on confirmBooking(), cancelBooking()
// WHY? → Decouples "booking confirmed/cancelled" from "notify user/send email".
//         New listeners (e.g., analytics, refund service) can be added without changing BookingSystem.

interface BookingObserver {            // interface = Observer pattern; notifies on booking events
    void onBookingConfirmed(Booking booking);
    void onBookingCancelled(Booking booking);
}
