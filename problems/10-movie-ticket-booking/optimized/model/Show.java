/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Show.java — Movie screening with ConcurrentHashMap seat status and ReentrantLock

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.List;

class Show {
    private String showId;
    private Movie movie;
    private Screen screen;
    private String timing;
    private ConcurrentHashMap<String, SeatStatus> seatStatuses; // ConcurrentHashMap = lock-free reads, thread-safe writes
    private ReentrantLock bookingLock;   // ReentrantLock = per-show mutual exclusion for multi-seat booking
    private AtomicInteger availableCount; // AtomicInteger = lock-free available seat counter

    public Show(String showId, Movie movie, Screen screen, String timing) {
        this.showId = showId; this.movie = movie; this.screen = screen; this.timing = timing;
        this.seatStatuses = new ConcurrentHashMap<>();
        this.bookingLock = new ReentrantLock();
        this.availableCount = new AtomicInteger(screen.getTotalSeats());
        for (Seat seat : screen.getSeats()) seatStatuses.put(seat.getSeatId(), SeatStatus.AVAILABLE);
    }

    public SeatStatus getSeatStatus(String seatId) { return seatStatuses.getOrDefault(seatId, SeatStatus.AVAILABLE); }

    public boolean setSeatStatus(String seatId, SeatStatus newStatus) {
        SeatStatus old = seatStatuses.put(seatId, newStatus);
        if (old == SeatStatus.AVAILABLE && newStatus != SeatStatus.AVAILABLE) availableCount.decrementAndGet();
        else if (old != SeatStatus.AVAILABLE && newStatus == SeatStatus.AVAILABLE) availableCount.incrementAndGet();
        return true;
    }

    public boolean tryBookSeats(List<String> seatIds) {
        bookingLock.lock();
        try {
            for (String seatId : seatIds) {
                SeatStatus status = seatStatuses.get(seatId);
                if (status == SeatStatus.BOOKED) return false;
            }
            for (String seatId : seatIds) setSeatStatus(seatId, SeatStatus.BOOKED);
            return true;
        } finally {
            bookingLock.unlock();
        }
    }

    public int getAvailableCount() { return availableCount.get(); }

    public int getAvailableCount(SeatType type) {
        int count = 0;
        for (Seat seat : screen.getSeats()) if (seat.getType() == type && seatStatuses.get(seat.getSeatId()) == SeatStatus.AVAILABLE) count++;
        return count;
    }

    public ReentrantLock getBookingLock() { return bookingLock; }
    public String getShowId() { return showId; }
    public Movie getMovie() { return movie; }
    public Screen getScreen() { return screen; }
    public String getTiming() { return timing; }

    @Override
    public String toString() { return movie.getTitle() + " - " + screen.getName() + ", " + timing + " (" + getAvailableCount() + "/" + screen.getTotalSeats() + " available)"; }
}
