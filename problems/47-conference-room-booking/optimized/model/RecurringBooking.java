/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/RecurringBooking.java — Composite of Booking leaves repeating on a schedule
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RecurringBooking implements BookingComponent { // implements BookingComponent = composite node aggregating leaf bookings
    public enum Frequency { DAILY, WEEKLY } // enum = fixed recurrence patterns; type-safe

    private String seriesId;            // private = unique series identifier
    private Frequency frequency;        // private = how often this booking repeats
    private int occurrences;            // private = total number of repetitions
    private List<Booking> bookings;     // private = all individual bookings in this series

    public RecurringBooking(String seriesId, Frequency frequency, int occurrences) {
        this.seriesId = seriesId; this.frequency = frequency;
        this.occurrences = occurrences; this.bookings = new ArrayList<>();
    }

    @Override public String getId() { return seriesId; }
    public List<Booking> getBookings() { return bookings; }
    public void addBooking(Booking booking) { bookings.add(booking); }

    // Composite cancel = cancel every leaf booking in the series
    @Override public void cancel() { for (Booking b : bookings) b.cancel(); }

    // Composite getTimeSlots = aggregate all leaf time slots
    @Override public List<TimeSlot> getTimeSlots() {
        List<TimeSlot> slots = new ArrayList<>();
        for (Booking b : bookings) slots.add(b.getTimeSlot());
        return slots;
    }

    public List<TimeSlot> generateTimeSlots(TimeSlot firstSlot) {
        List<TimeSlot> slots = new ArrayList<>();
        for (int i = 0; i < occurrences; i++) {
            LocalDateTime start, end;
            if (frequency == Frequency.DAILY) { start = firstSlot.getStart().plusDays(i); end = firstSlot.getEnd().plusDays(i); }
            else { start = firstSlot.getStart().plusWeeks(i); end = firstSlot.getEnd().plusWeeks(i); }
            slots.add(new TimeSlot(start, end));
        }
        return slots;
    }
}
