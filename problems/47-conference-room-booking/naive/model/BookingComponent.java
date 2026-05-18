/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/BookingComponent.java — Composite interface; treats single Booking and RecurringBooking series uniformly
import java.util.List;

public interface BookingComponent { // interface = Composite root; both leaf (Booking) and composite (RecurringBooking) implement this
    String getId();                  // unique identifier (booking id or series id)
    List<TimeSlot> getTimeSlots();   // one slot for a leaf, many slots for a series
    void cancel();                   // cancel the leaf booking, or cancel every booking in a series
}
