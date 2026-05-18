/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/BookingObserver.java — Observer pattern interface for booking lifecycle events
// DESIGN PATTERN: Observer
//
// WHO IMPLEMENTS THIS? → NotificationService (in NotificationService.java)
// WHO CALLS IT? → Hotel calls observers on bookRoom(), checkIn(), checkOut()
// WHY? → Decouples "booking happened" from "notify guest/send email".
//         New listeners (e.g., analytics, loyalty points) can be added without changing Hotel code.

interface BookingObserver {            // interface = contract for event listeners (Observer pattern)
    void onBookingConfirmed(Booking booking);
    void onCheckIn(Booking booking);
    void onCheckOut(Booking booking);
}
