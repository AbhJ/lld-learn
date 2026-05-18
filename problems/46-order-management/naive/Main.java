/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the order management system
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Order Management Demo (Naive) ===");

        OrderService service = new OrderService();

        OrderItem laptop = new OrderItem("p1", "Laptop", 999.99, 1);
        OrderItem mouse = new OrderItem("p2", "Mouse", 29.99, 2);

        System.out.println("\n--- Create Order ---");
        Order order = service.createOrder(Arrays.asList(laptop, mouse));
        System.out.println("Order " + order.getId() + " total: $" + String.format("%.2f", order.getItemsTotal()));

        System.out.println("\n--- Invalid Transitions ---");
        order.ship(new ExpressShipping(), "TRK-000");
        order.deliver();

        System.out.println("\n--- Payment ---");
        order.pay(new Payment("PAY-001", order.getItemsTotal(), Payment.PaymentMethod.CREDIT_CARD));

        System.out.println("\n--- Shipping ---");
        order.ship(new ExpressShipping(), "TRK-123");

        System.out.println("\n--- Delivery ---");
        order.deliver();

        System.out.println("\n--- Return & Refund ---");
        Return ret = order.initiateReturn(mouse, Return.ReturnReason.DEFECTIVE);
        if (ret != null) order.processRefund(ret);

        System.out.println("\n--- Order History ---");
        order.getHistory().printTimeline();

        System.out.println("\n=== Order Management Demo Complete ===");
    }
}
