/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Bill.java — Aggregates room and service charges into a final invoice

import java.time.LocalDate;

class Bill {
    private Booking booking;
    private double roomCharges;
    private double serviceCharges;
    private PricingStrategy pricingStrategy;

    public Bill(Booking booking, RoomService roomService, PricingStrategy pricingStrategy) {
        this.booking = booking;
        this.pricingStrategy = pricingStrategy;
        this.roomCharges = pricingStrategy.calculatePrice(booking.getRoom(), booking.getCheckInDate(), booking.getNights());
        this.serviceCharges = (roomService != null) ? roomService.getTotal() : 0;
    }

    public double getTotal() { return roomCharges + serviceCharges; }

    public String generateInvoice() {
        StringBuilder sb = new StringBuilder();
        sb.append("  Guest: ").append(booking.getGuest().getName()).append("\n");
        sb.append("  Room: ").append(booking.getRoom().getRoomNumber()).append(" (").append(booking.getRoom().getRoomType()).append(")\n");
        sb.append("  Period: ").append(booking.getCheckInDate()).append(" to ").append(booking.getCheckOutDate()).append("\n");
        sb.append("  Room Charges:    $").append(String.format("%.2f", roomCharges)).append("\n");
        sb.append("  Service Charges: $").append(String.format("%.2f", serviceCharges)).append("\n");
        sb.append("  TOTAL:           $").append(String.format("%.2f", getTotal()));
        return sb.toString();
    }
}
