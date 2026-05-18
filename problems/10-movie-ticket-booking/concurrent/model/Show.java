/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Show.java — A movie show with seats

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class Show {
    private final String showId;        // final = immutable; safe publication
    private final String movieName;     // final = immutable; safe to read from any thread
    private final String time;          // final = immutable; safe publication
    private final ConcurrentHashMap<String, Seat> seats; // ConcurrentHashMap = thread-safe O(1) seat lookup by ID

    public Show(String showId, String movieName, String time, int rows, int cols) {
        this.showId = showId;
        this.movieName = movieName;
        this.time = time;
        this.seats = new ConcurrentHashMap<>();

        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                String seatId = (char) ('A' + r - 1) + String.valueOf(c);
                seats.put(seatId, new Seat(seatId, r, c));
            }
        }
    }

    public String getShowId() { return showId; }
    public String getMovieName() { return movieName; }
    public String getTime() { return time; }

    public Seat getSeat(String seatId) {
        return seats.get(seatId);
    }

    public List<Seat> getAvailableSeats() {
        List<Seat> available = new ArrayList<>();
        for (Seat seat : seats.values()) {
            if (seat.getStatus() == SeatStatus.AVAILABLE) {
                available.add(seat);
            }
        }
        return available;
    }

    public int getTotalSeats() { return seats.size(); }

    public long getBookedCount() {
        return seats.values().stream()
                .filter(s -> s.getStatus() == SeatStatus.BOOKED)
                .count();
    }
}
