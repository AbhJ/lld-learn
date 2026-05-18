/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/PaymentProcessor.java — Processes payments by delegating fee calculation to the pricing strategy

class PaymentProcessor {
    private PricingStrategy strategy; // private = swappable strategy hidden; changed via setStrategy()

    public PaymentProcessor(PricingStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(PricingStrategy strategy) {
        this.strategy = strategy;
    }

    public PricingStrategy getStrategy() {
        return strategy;
    }

    public double processPayment(Ticket ticket) {
        double fee = strategy.calculateFee(ticket);
        return fee;
    }

    public String generateReceipt(Ticket ticket) {
        double fee = strategy.calculateFee(ticket);
        StringBuilder sb = new StringBuilder();
        sb.append("=== Payment Receipt ===\n");
        sb.append("Ticket: ").append(ticket.getTicketId()).append("\n");
        sb.append("Vehicle: ").append(ticket.getVehicle()).append("\n");
        sb.append("Spot: ").append(ticket.getSpot()).append("\n");
        sb.append("Duration: ").append(ticket.getDurationInHours()).append(" hour(s)\n");
        sb.append("Pricing: ").append(strategy.getName()).append("\n");
        sb.append("Amount: $").append(String.format("%.2f", fee)).append("\n");
        sb.append("========================");
        return sb.toString();
    }
}
