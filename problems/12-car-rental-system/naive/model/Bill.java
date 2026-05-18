/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Bill.java — Rental bill with itemized charges

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Bill {
    private static int counter = 0;       // static = shared across all Bills; class-level counter
    private String billId;                // private = encapsulated unique bill identifier
    private Reservation reservation;      // private = the reservation this bill is for
    private LocalDateTime generatedAt;    // private = when the bill was created

    public Bill(Reservation reservation) {
        this.billId = "BILL-" + (++counter);
        this.reservation = reservation;
        this.generatedAt = LocalDateTime.now();
    }

    public String getBillId() { return billId; }
    public double getTotal() { return reservation.getTotalCost(); }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append("============= BILL =============\n");
        sb.append("Bill #: ").append(billId).append("\n");
        sb.append("Date: ").append(generatedAt.format(fmt)).append("\n");
        sb.append("Reservation: ").append(reservation.getReservationId()).append("\n");
        sb.append("Customer: ").append(reservation.getCustomer().getName()).append("\n");
        sb.append("Vehicle: ").append(reservation.getVehicle().getMake())
                .append(" ").append(reservation.getVehicle().getModel())
                .append(" (").append(reservation.getVehicle().getType()).append(")\n");
        sb.append("Duration: ").append(reservation.getDays()).append(" days\n");
        sb.append("Pricing: ").append(reservation.getPricingStrategy().getName()).append("\n");
        sb.append("Pickup: ").append(reservation.getPickupLocation()).append("\n");
        sb.append("Dropoff: ").append(reservation.getDropoffLocation()).append("\n");
        sb.append("--------------------------------\n");
        sb.append(String.format("Vehicle Cost:    $%.2f\n", reservation.getVehicleCost()));
        for (Insurance ins : reservation.getInsurances()) {
            sb.append(String.format("  %s Insurance: $%.2f\n", ins.getType(), ins.getCost(reservation.getDays())));
        }
        sb.append(String.format("Insurance Total: $%.2f\n", reservation.getInsuranceCost()));
        sb.append("--------------------------------\n");
        sb.append(String.format("TOTAL:           $%.2f\n", reservation.getTotalCost()));
        sb.append("================================");
        return sb.toString();
    }
}
