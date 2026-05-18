/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Show.java — Movie screening with per-show seat availability (synchronized)

import java.util.HashMap;
import java.util.Map;

class Show {
    private String showId;              // private = show ID encapsulated
    private Movie movie;                // private = movie reference encapsulated
    private Screen screen;              // private = screen reference encapsulated
    private String timing;              // private = showtime encapsulated
    private Map<String, SeatStatus> seatStatuses; // private = per-seat status managed internally

    public Show(String showId, Movie movie, Screen screen, String timing) {
        this.showId = showId; this.movie = movie; this.screen = screen; this.timing = timing;
        this.seatStatuses = new HashMap<>();
        for (Seat seat : screen.getSeats()) seatStatuses.put(seat.getSeatId(), SeatStatus.AVAILABLE);
    }

    public synchronized SeatStatus getSeatStatus(String seatId) { return seatStatuses.getOrDefault(seatId, SeatStatus.AVAILABLE); } // synchronized = one thread at a time reads status
    public synchronized boolean setSeatStatus(String seatId, SeatStatus status) { seatStatuses.put(seatId, status); return true; } // synchronized = one thread at a time writes status

    public int getAvailableCount() {
        int count = 0;
        for (SeatStatus s : seatStatuses.values()) if (s == SeatStatus.AVAILABLE) count++;
        return count;
    }

    public int getAvailableCount(SeatType type) {
        int count = 0;
        for (Seat seat : screen.getSeats()) if (seat.getType() == type && seatStatuses.get(seat.getSeatId()) == SeatStatus.AVAILABLE) count++;
        return count;
    }

    public String getShowId() { return showId; }
    public Movie getMovie() { return movie; }
    public Screen getScreen() { return screen; }
    public String getTiming() { return timing; }

    @Override
    public String toString() { return movie.getTitle() + " - " + screen.getName() + ", " + timing + " (" + getAvailableCount() + "/" + screen.getTotalSeats() + " available)"; }
}
