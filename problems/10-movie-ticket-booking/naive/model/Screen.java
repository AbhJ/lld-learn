/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Screen.java — Cinema screen with seat layout configuration

import java.util.ArrayList;
import java.util.List;

class Screen {
    private String screenId;            // private = screen ID encapsulated
    private String name;                // private = screen name encapsulated
    private List<Seat> seats;           // private = seat layout managed internally

    public Screen(String screenId, String name) {
        this.screenId = screenId; this.name = name; this.seats = new ArrayList<>();
    }

    public void setupSeats(int regularRows, int premiumRows, int vipRows, int seatsPerRow) {
        int rowNum = 0;
        for (int r = 0; r < vipRows; r++) { char rc = (char)('A' + rowNum); for (int c = 1; c <= seatsPerRow; c++) seats.add(SeatFactory.createSeat(SeatType.VIP, rc + "" + c, rowNum, c)); rowNum++; }
        for (int r = 0; r < premiumRows; r++) { char rc = (char)('A' + rowNum); for (int c = 1; c <= seatsPerRow; c++) seats.add(SeatFactory.createSeat(SeatType.PREMIUM, rc + "" + c, rowNum, c)); rowNum++; }
        for (int r = 0; r < regularRows; r++) { char rc = (char)('A' + rowNum); for (int c = 1; c <= seatsPerRow; c++) seats.add(SeatFactory.createSeat(SeatType.REGULAR, rc + "" + c, rowNum, c)); rowNum++; }
    }

    public Seat getSeat(String seatId) {
        for (Seat seat : seats) if (seat.getSeatId().equals(seatId)) return seat;
        return null;
    }

    public List<Seat> getSeats() { return seats; }
    public String getScreenId() { return screenId; }
    public String getName() { return name; }
    public int getTotalSeats() { return seats.size(); }

    @Override
    public String toString() { return name + " (" + seats.size() + " seats)"; }
}
