/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Screen.java — Cinema screen with seat layout and HashMap-indexed lookup

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Screen {
    private String screenId;
    private String name;
    private List<Seat> seats;           // ArrayList = ordered seat collection for display
    private Map<String, Seat> seatIndex; // HashMap = O(1) seat lookup by ID vs linear scan

    public Screen(String screenId, String name) {
        this.screenId = screenId; this.name = name; this.seats = new ArrayList<>(); this.seatIndex = new HashMap<>();
    }

    public void setupSeats(int regularRows, int premiumRows, int vipRows, int seatsPerRow) {
        int rowNum = 0;
        for (int r = 0; r < vipRows; r++) { char rc = (char)('A' + rowNum); for (int c = 1; c <= seatsPerRow; c++) { Seat s = SeatFactory.createSeat(SeatType.VIP, rc + "" + c, rowNum, c); seats.add(s); seatIndex.put(s.getSeatId(), s); } rowNum++; }
        for (int r = 0; r < premiumRows; r++) { char rc = (char)('A' + rowNum); for (int c = 1; c <= seatsPerRow; c++) { Seat s = SeatFactory.createSeat(SeatType.PREMIUM, rc + "" + c, rowNum, c); seats.add(s); seatIndex.put(s.getSeatId(), s); } rowNum++; }
        for (int r = 0; r < regularRows; r++) { char rc = (char)('A' + rowNum); for (int c = 1; c <= seatsPerRow; c++) { Seat s = SeatFactory.createSeat(SeatType.REGULAR, rc + "" + c, rowNum, c); seats.add(s); seatIndex.put(s.getSeatId(), s); } rowNum++; }
    }

    public Seat getSeat(String seatId) { return seatIndex.get(seatId); }
    public List<Seat> getSeats() { return seats; }
    public String getScreenId() { return screenId; }
    public String getName() { return name; }
    public int getTotalSeats() { return seats.size(); }

    @Override
    public String toString() { return name + " (" + seats.size() + " seats)"; }
}
