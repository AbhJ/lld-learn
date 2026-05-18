/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Reservation.java — Vehicle reservation with dates, location, and status tracking

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Reservation {
    public enum Status { PENDING, CONFIRMED, ACTIVE, COMPLETED, CANCELLED } // enum = fixed states

    private String reservationId;            // unique reservation ID
    private Customer customer;               // who made the reservation
    private Vehicle vehicle;                 // deindexed from PriorityQueue when reserved
    private LocalDate startDate;             // rental start date
    private LocalDate endDate;               // rental end date
    private Location pickupLocation;         // pickup branch
    private Location dropoffLocation;        // dropoff branch; re-indexes vehicle there
    private List<Insurance> insurances;      // ArrayList of selected insurance options
    private PricingStrategy pricingStrategy; // strategy pattern for fare calculation
    private Status status;                   // lifecycle state (enum)
    private int days;                        // computed duration for pricing

    private Reservation(Builder builder) {
        this.reservationId = builder.reservationId;
        this.customer = builder.customer;
        this.vehicle = builder.vehicle;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.pickupLocation = builder.pickupLocation;
        this.dropoffLocation = builder.dropoffLocation;
        this.insurances = builder.insurances;
        this.pricingStrategy = builder.pricingStrategy;
        this.status = Status.CONFIRMED;
        this.days = (int) (endDate.toEpochDay() - startDate.toEpochDay());
    }

    public String getReservationId() { return reservationId; }
    public Customer getCustomer() { return customer; }
    public Vehicle getVehicle() { return vehicle; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Location getPickupLocation() { return pickupLocation; }
    public Location getDropoffLocation() { return dropoffLocation; }
    public List<Insurance> getInsurances() { return insurances; }
    public PricingStrategy getPricingStrategy() { return pricingStrategy; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public int getDays() { return days; }

    public double getVehicleCost() {
        return pricingStrategy.calculatePrice(vehicle, days);
    }

    public double getInsuranceCost() {
        return insurances.stream().mapToDouble(i -> i.getCost(days)).sum();
    }

    public double getTotalCost() {
        return getVehicleCost() + getInsuranceCost();
    }

    @Override
    public String toString() {
        return String.format("Reservation[%s] %s -> %s, %s, %d days, Status: %s",
                reservationId, customer.getName(), vehicle, pickupLocation.getName(), days, status);
    }

    // Builder
    public static class Builder {
        private static int counter = 0;
        private String reservationId;
        private Customer customer;
        private Vehicle vehicle;
        private LocalDate startDate;
        private LocalDate endDate;
        private Location pickupLocation;
        private Location dropoffLocation;
        private List<Insurance> insurances = new ArrayList<>();
        private PricingStrategy pricingStrategy = new DailyPricing();

        public Builder(Customer customer, Vehicle vehicle) {
            this.reservationId = "RES-" + (++counter);
            this.customer = customer;
            this.vehicle = vehicle;
        }

        public Builder startDate(LocalDate date) { this.startDate = date; return this; }
        public Builder endDate(LocalDate date) { this.endDate = date; return this; }
        public Builder pickupLocation(Location loc) { this.pickupLocation = loc; return this; }
        public Builder dropoffLocation(Location loc) { this.dropoffLocation = loc; return this; }
        public Builder addInsurance(Insurance ins) { this.insurances.add(ins); return this; }
        public Builder pricingStrategy(PricingStrategy strategy) { this.pricingStrategy = strategy; return this; }

        public Reservation build() {
            if (startDate == null || endDate == null || pickupLocation == null) {
                throw new IllegalStateException("Start date, end date, and pickup location are required");
            }
            if (dropoffLocation == null) dropoffLocation = pickupLocation;
            return new Reservation(this);
        }
    }
}
