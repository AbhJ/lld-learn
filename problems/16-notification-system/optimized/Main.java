/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating async notification system with per-channel queues

import java.util.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Notification System (Optimized) ===\n");

        AsyncNotificationService service = new AsyncNotificationService(3);
        service.registerChannel(new EmailChannel());
        service.registerChannel(new SMSChannel());
        service.registerChannel(new PushChannel());

        // --- Test 1: Synchronous Send (for demo visibility) ---
        System.out.println("--- Test 1: Basic Email Notification ---");
        Notification n1 = NotificationFactory.createEmailNotification(
                "U-1", "alice@example.com", "Order Shipped",
                "Your order #1234 has been shipped!", Priority.HIGH);
        service.sendSync(n1);
        System.out.println();

        // --- Test 2: SMS ---
        System.out.println("--- Test 2: SMS Notification ---");
        Notification n2 = NotificationFactory.createSMSNotification(
                "U-1", "+1-555-0101", "Your OTP is 123456.", Priority.CRITICAL);
        service.sendSync(n2);
        System.out.println();

        // --- Test 3: Push ---
        System.out.println("--- Test 3: Push Notification ---");
        Notification n3 = NotificationFactory.createPushNotification(
                "U-2", "device_abc123", "New Message", "You have a new message", Priority.MEDIUM);
        service.sendSync(n3);
        System.out.println();

        // --- Test 4: Template ---
        System.out.println("--- Test 4: Template-based Notification ---");
        Template orderTemplate = new Template("T-1", "Order Confirmation",
                "Hi {{name}}, your order #{{orderId}} of ${{amount}} has been confirmed.");
        Map<String, String> vars = new HashMap<>();
        vars.put("name", "Bob");
        vars.put("orderId", "5678");
        vars.put("amount", "99.99");
        Notification n4 = NotificationFactory.fromTemplate("U-2", "bob@example.com", "EMAIL",
                orderTemplate, vars, Priority.HIGH);
        service.sendSync(n4);
        System.out.println();

        // --- Test 5: Rate Limiting ---
        System.out.println("--- Test 5: Rate Limiting (3/minute per user+channel) ---");
        service.sendSync(NotificationFactory.createEmailNotification("U-1", "alice@example.com", "Promo1", "Sale!", Priority.LOW));
        service.sendSync(NotificationFactory.createEmailNotification("U-1", "alice@example.com", "Promo2", "More!", Priority.LOW));
        // This should be throttled (3rd EMAIL for U-1, first was n1)
        service.sendSync(NotificationFactory.createEmailNotification("U-1", "alice@example.com", "Promo3", "Extra!", Priority.LOW));
        System.out.println();

        // --- Test 6: Priority Batch ---
        System.out.println("--- Test 6: Priority-based Batch (CRITICAL first) ---");
        List<Notification> batch = new ArrayList<>();
        batch.add(NotificationFactory.createEmailNotification("U-3", "u3@test.com", "Low", "Newsletter", Priority.LOW));
        batch.add(NotificationFactory.createSMSNotification("U-3", "+1-555-0303", "CRITICAL alert!", Priority.CRITICAL));
        batch.add(NotificationFactory.createPushNotification("U-3", "dev_xyz", "Medium", "Update", Priority.MEDIUM));
        service.sendBatch(batch);
        System.out.println();

        // --- Test 7: Statistics ---
        System.out.println("--- Test 7: Statistics ---");
        System.out.println("  Sent: " + service.getSentCount());
        System.out.println("  Failed: " + service.getFailedCount());
        System.out.println("  Throttled: " + service.getThrottledCount());
        System.out.println("  Total: " + service.getHistory().size());
        System.out.println();

        service.shutdown();
        System.out.println("=== Notification System Demo Complete ===");
    }
}
