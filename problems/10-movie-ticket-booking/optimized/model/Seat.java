/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Seat.java — Theater seat with type-based pricing tiers

enum SeatType { REGULAR, PREMIUM, VIP }
enum SeatStatus { AVAILABLE, LOCKED, BOOKED }

abstract class Seat {
    protected String seatId;
    protected int row;
    protected int col;
    protected SeatType type;
    protected double price;

    public Seat(String seatId, int row, int col, SeatType type, double price) {
        this.seatId = seatId; this.row = row; this.col = col; this.type = type; this.price = price;
    }

    public String getSeatId() { return seatId; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public SeatType getType() { return type; }
    public double getPrice() { return price; }

    @Override
    public String toString() { return seatId + "(" + type + ",$" + String.format("%.0f", price) + ")"; }
}

class RegularSeat extends Seat { public RegularSeat(String seatId, int row, int col) { super(seatId, row, col, SeatType.REGULAR, 10.0); } }
class PremiumSeat extends Seat { public PremiumSeat(String seatId, int row, int col) { super(seatId, row, col, SeatType.PREMIUM, 15.0); } }
class VIPSeat extends Seat { public VIPSeat(String seatId, int row, int col) { super(seatId, row, col, SeatType.VIP, 25.0); } }

class SeatFactory {
    public static Seat createSeat(SeatType type, String seatId, int row, int col) {
        switch (type) {
            case REGULAR: return new RegularSeat(seatId, row, col);
            case PREMIUM: return new PremiumSeat(seatId, row, col);
            case VIP: return new VIPSeat(seatId, row, col);
            default: throw new IllegalArgumentException("Unknown seat type");
        }
    }
}
