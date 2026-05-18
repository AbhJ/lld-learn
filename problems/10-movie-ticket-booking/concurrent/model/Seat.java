/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Seat.java — Seat with atomic status for thread-safe booking

import java.util.concurrent.atomic.AtomicReference;

enum SeatStatus {
    AVAILABLE, LOCKED, BOOKED
}

class Seat {
    private final String seatId;        // final = immutable; safe to read from any thread
    private final int row;              // final = immutable; no sync needed
    private final int column;           // final = immutable; no sync needed
    private final AtomicReference<SeatStatus> status = new AtomicReference<>(SeatStatus.AVAILABLE); // AtomicReference = CAS-based state transition; prevents double-booking

    public Seat(String seatId, int row, int column) {
        this.seatId = seatId;
        this.row = row;
        this.column = column;
    }

    public String getSeatId() { return seatId; }
    public int getRow() { return row; }
    public int getColumn() { return column; }
    public SeatStatus getStatus() { return status.get(); }

    /**
     * CAS-based lock attempt — only one thread can lock a seat.
     */
    public boolean tryLock() {
        return status.compareAndSet(SeatStatus.AVAILABLE, SeatStatus.LOCKED); // CAS = atomic AVAILABLE->LOCKED
    }

    public boolean confirmBooking() {
        return status.compareAndSet(SeatStatus.LOCKED, SeatStatus.BOOKED); // CAS = atomic LOCKED->BOOKED
    }

    public boolean release() {
        return status.compareAndSet(SeatStatus.LOCKED, SeatStatus.AVAILABLE); // CAS = atomic rollback
    }

    @Override
    public String toString() {
        return seatId + " [" + status.get() + "]";
    }
}
