/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/SeatMap.java — BitSet-based seat availability for O(1) check/mark operations
import java.util.BitSet;

public class SeatMap {
    // WHY BitSet: O(1) to check/mark a seat, O(seats/64) to find first available.
    // Naive linear search is O(n) per booking. BitSet uses 1 bit per seat vs.
    // full Seat objects — 64x less memory, CPU-cache-friendly for scanning.
    private final BitSet booked;           // BitSet = 1 bit per seat; compact availability bitmap
    private final String[] labels;         // String[] = seat labels indexed by position
    private final String[] classes;        // String[] = seat class per index
    private final int totalSeats;          // final = total capacity fixed at creation

    // Class boundaries for O(1) class-range lookup
    private final int firstStart, firstEnd;       // final = range bounds for first-class seats
    private final int businessStart, businessEnd; // final = range bounds for business seats
    private final int economyStart, economyEnd;   // final = range bounds for economy seats

    public SeatMap(int firstCount, int businessCount, int economyCount) {
        this.totalSeats = firstCount + businessCount + economyCount;
        this.booked = new BitSet(totalSeats);
        this.labels = new String[totalSeats];
        this.classes = new String[totalSeats];

        this.firstStart = 0;
        this.firstEnd = firstCount;
        this.businessStart = firstCount;
        this.businessEnd = firstCount + businessCount;
        this.economyStart = firstCount + businessCount;
        this.economyEnd = totalSeats;

        int idx = 0;
        for (int i = 0; i < firstCount; i++) { labels[idx] = (idx+1) + "F"; classes[idx] = "FIRST"; idx++; }
        for (int i = 0; i < businessCount; i++) { labels[idx] = (idx+1) + "B"; classes[idx] = "BUSINESS"; idx++; }
        for (int i = 0; i < economyCount; i++) { labels[idx] = (idx+1) + "E"; classes[idx] = "ECONOMY"; idx++; }
    }

    // WHY nextClearBit: BitSet.nextClearBit() uses native word scanning — finds first
    // available seat in the class range in O(range/64) instead of O(range).
    public int bookFirstAvailable(String seatClass) {
        int start, end;
        switch (seatClass) {
            case "FIRST": start = firstStart; end = firstEnd; break;
            case "BUSINESS": start = businessStart; end = businessEnd; break;
            default: start = economyStart; end = economyEnd; break;
        }

        int idx = booked.nextClearBit(start);
        if (idx >= end) return -1; // No available seat in this class

        booked.set(idx);
        return idx;
    }

    public void release(int seatIndex) {
        booked.clear(seatIndex);
    }

    public boolean isAvailable(int idx) { return !booked.get(idx); }
    public String getLabel(int idx) { return labels[idx]; }
    public String getSeatClass(int idx) { return classes[idx]; }

    public int getAvailableCount(String seatClass) {
        int start, end;
        switch (seatClass) {
            case "FIRST": start = firstStart; end = firstEnd; break;
            case "BUSINESS": start = businessStart; end = businessEnd; break;
            default: start = economyStart; end = economyEnd; break;
        }
        int count = 0;
        for (int i = booked.nextClearBit(start); i < end; i = booked.nextClearBit(i + 1)) count++;
        return count;
    }

    public int getTotalSeats() { return totalSeats; }
    public int getBookedCount() { return booked.cardinality(); }
}
