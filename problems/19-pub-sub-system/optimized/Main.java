/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating per-topic threading and at-least-once delivery

import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Pub-Sub System (Optimized) ===\n");

        MessageBroker broker = new MessageBroker(DeliveryGuarantee.AT_LEAST_ONCE);
        broker.createTopic("orders");
        broker.createTopic("notifications");
        broker.createTopic("analytics");

        ConcreteSubscriber orderProcessor = new ConcreteSubscriber("S-1", "OrderProcessor");
        ConcreteSubscriber analytics = new ConcreteSubscriber("S-2", "AnalyticsService");
        ConcreteSubscriber notifier = new ConcreteSubscriber("S-3", "NotificationService");

        broker.subscribe("orders", orderProcessor);
        broker.subscribe("orders", analytics);
        broker.subscribe("notifications", notifier);
        System.out.println();

        // --- Test 1: Publishing ---
        System.out.println("--- Test 1: Publishing Messages ---");
        Publisher orderService = new Publisher("P-1", "OrderService", broker);
        Publisher userService = new Publisher("P-2", "UserService", broker);

        Message m1 = orderService.publish("orders", "{\"orderId\": 123, \"amount\": 99.99}");
        Message m2 = orderService.publish("orders", "{\"orderId\": 124, \"amount\": 25.00}");
        userService.publish("notifications", "Welcome email sent to alice@test.com");
        System.out.println();

        // --- Test 2: Content Filtering ---
        System.out.println("--- Test 2: Content-Based Filtering ---");
        ConcreteSubscriber premiumHandler = new ConcreteSubscriber("S-4", "PremiumHandler",
                new PayloadContainsFilter("premium"));
        broker.subscribe("orders", premiumHandler);
        orderService.publish("orders", "{\"orderId\": 125, \"type\": \"premium\", \"amount\": 500}");
        orderService.publish("orders", "{\"orderId\": 126, \"type\": \"regular\"}");
        System.out.println();

        // --- Test 3: At-Least-Once Acknowledgment ---
        System.out.println("--- Test 3: At-Least-Once Ack Tracking ---");
        System.out.println("OrderProcessor unacked: " + broker.getUnackedCount(orderProcessor));
        broker.acknowledgeMessage(orderProcessor, m1.getMessageId());
        broker.acknowledgeMessage(orderProcessor, m2.getMessageId());
        System.out.println("After acking 2 messages:");
        System.out.println("  Acked: " + broker.getAckedCount(orderProcessor));
        System.out.println("  Unacked: " + broker.getUnackedCount(orderProcessor));
        System.out.println();

        // --- Test 4: Header Filtering ---
        System.out.println("--- Test 4: Header-Based Filtering ---");
        ConcreteSubscriber urgentHandler = new ConcreteSubscriber("S-5", "UrgentHandler",
                new HeaderFilter("priority", "urgent"));
        broker.subscribe("notifications", urgentHandler);

        Map<String, String> normalHeaders = new HashMap<>();
        normalHeaders.put("priority", "normal");
        userService.publish("notifications", "Regular update", normalHeaders);

        Map<String, String> urgentHeaders = new HashMap<>();
        urgentHeaders.put("priority", "urgent");
        userService.publish("notifications", "URGENT: Security breach!", urgentHeaders);
        System.out.println();

        // --- Test 5: Unsubscribe ---
        System.out.println("--- Test 5: Unsubscribe ---");
        System.out.println("Subscribers on 'orders': " + broker.getSubscriberCount("orders"));
        broker.unsubscribe("orders", analytics);
        System.out.println("After unsubscribe: " + broker.getSubscriberCount("orders"));
        orderService.publish("orders", "{\"orderId\": 127, \"type\": \"test\"}");
        System.out.println();

        // --- Test 6: Statistics ---
        System.out.println("--- Test 6: Broker Statistics ---");
        System.out.println("Total topics: " + broker.getTopicCount());
        System.out.println("Total published: " + broker.getTotalPublished());
        System.out.println("Total deliveries: " + broker.getTotalDelivered());
        System.out.println("OrderProcessor received: " + orderProcessor.getMessagesReceived());
        System.out.println();

        broker.shutdown();
        System.out.println("=== Pub-Sub Demo Complete ===");
    }
}
