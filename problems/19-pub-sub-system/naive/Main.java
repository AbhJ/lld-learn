/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the pub-sub system

/*
 * VARIATIONS FREQUENTLY ASKED:
 * 1. Distributed pub-sub (Kafka-like) - Partitions, consumer groups, offset management
 * 2. Dead letter queue - Failed messages, retry with backoff, poison pill handling
 * 3. Message ordering - Per-key ordering, sequence numbers, resequencing
 * 4. Fan-out/fan-in - One-to-many delivery, aggregation patterns
 * 5. Schema evolution - Backward/forward compatibility, schema registry
 *
 * See VARIATIONS.md for full solution approaches.
 */
import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Pub-Sub Messaging System Demo ===\n");

        // --- Test 1: Basic Pub-Sub ---
        System.out.println("--- Test 1: Basic Publish-Subscribe ---");
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

        // --- Test 2: Publishing Messages ---
        System.out.println("--- Test 2: Publishing Messages ---");
        Publisher orderService = new Publisher("P-1", "OrderService", broker);
        Publisher userService = new Publisher("P-2", "UserService", broker);

        System.out.println("Publishing order event:");
        orderService.publish("orders", "{\"orderId\": 123, \"amount\": 99.99, \"type\": \"purchase\"}");
        System.out.println();

        System.out.println("Publishing notification:");
        userService.publish("notifications", "Welcome email sent to new user alice@test.com");
        System.out.println();

        // --- Test 3: Content-Based Filtering ---
        System.out.println("--- Test 3: Content-Based Filtering ---");
        ConcreteSubscriber highValueProcessor = new ConcreteSubscriber("S-4", "HighValueOrders",
                new PayloadContainsFilter("premium"));
        broker.subscribe("orders", highValueProcessor);
        System.out.println();

        System.out.println("Publishing regular order:");
        orderService.publish("orders", "{\"orderId\": 124, \"amount\": 25.00, \"type\": \"regular\"}");
        System.out.println();

        System.out.println("Publishing premium order:");
        orderService.publish("orders", "{\"orderId\": 125, \"amount\": 500.00, \"type\": \"premium\"}");
        System.out.println();

        // --- Test 4: Header-Based Filtering ---
        System.out.println("--- Test 4: Header-Based Filtering ---");
        ConcreteSubscriber urgentHandler = new ConcreteSubscriber("S-5", "UrgentHandler",
                new HeaderFilter("priority", "urgent"));
        broker.subscribe("notifications", urgentHandler);
        System.out.println();

        Map<String, String> headers1 = new HashMap<>();
        headers1.put("priority", "normal");
        userService.publish("notifications", "Regular update", headers1);

        Map<String, String> headers2 = new HashMap<>();
        headers2.put("priority", "urgent");
        userService.publish("notifications", "URGENT: Security breach detected!", headers2);
        System.out.println();

        // --- Test 5: Message Acknowledgment ---
        System.out.println("--- Test 5: Message Acknowledgment ---");
        MessageQueue queue = broker.getQueue(orderProcessor);
        System.out.println("OrderProcessor queue size: " + queue.size());
        System.out.println("Acknowledged count: " + queue.getAcknowledgedCount());

        // Acknowledge messages
        Message msg = queue.dequeue();
        if (msg != null) broker.acknowledgeMessage(orderProcessor, msg.getMessageId());
        msg = queue.dequeue();
        if (msg != null) broker.acknowledgeMessage(orderProcessor, msg.getMessageId());

        System.out.println("After acknowledgment - Queue size: " + queue.size());
        System.out.println("Acknowledged count: " + queue.getAcknowledgedCount());
        System.out.println();

        // --- Test 6: Unsubscribe ---
        System.out.println("--- Test 6: Unsubscribe ---");
        System.out.println("Subscribers on 'orders' before: " + broker.getSubscriberCount("orders"));
        broker.unsubscribe("orders", analytics);
        System.out.println("Subscribers on 'orders' after: " + broker.getSubscriberCount("orders"));
        System.out.println("\nPublishing after unsubscribe:");
        orderService.publish("orders", "{\"orderId\": 126, \"type\": \"test\"}");
        System.out.println();

        // --- Test 7: Multiple Topics ---
        System.out.println("--- Test 7: Multi-Topic Subscriber ---");
        ConcreteSubscriber auditLog = new ConcreteSubscriber("S-6", "AuditLog");
        broker.subscribe("orders", auditLog);
        broker.subscribe("notifications", auditLog);
        broker.subscribe("analytics", auditLog);
        System.out.println();

        orderService.publish("orders", "Order 127 placed");
        userService.publish("notifications", "User logged in");
        orderService.publish("analytics", "Page view: /checkout");
        System.out.println();

        // --- Test 8: Statistics ---
        System.out.println("--- Test 8: Broker Statistics ---");
        System.out.println("Total topics: " + broker.getTopicCount());
        System.out.println("Total messages published: " + broker.getTotalPublished());
        System.out.println("Total deliveries: " + broker.getTotalDelivered());
        System.out.println("Orders topic messages: " + broker.getTopic("orders").getMessageCount());
        System.out.println("OrderProcessor messages received: " + orderProcessor.getMessagesReceived());
        System.out.println();

        System.out.println("=== Pub-Sub System Demo Complete ===");
    }
}
