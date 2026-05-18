/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the notification system

/*
 * VARIATIONS FREQUENTLY ASKED:
 * 1. Preference management - Per-channel opt-in/out, quiet hours, digest mode
 * 2. A/B testing notifications - Different copy, timing, measure open rates
 * 3. Localization - Multi-language templates, timezone-aware delivery
 * 4. Batch/digest mode - Aggregate notifications, daily summary email
 * 5. Rich notifications - Images, action buttons, deep links, carousel
 *
 * See VARIATIONS.md for full solution approaches.
 */
import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Notification System Demo ===\n");

        // Setup service with throttle limit of 3 per minute
        NotificationService service = new NotificationService(3);
        EmailChannel emailChannel = new EmailChannel();
        service.registerChannel(emailChannel);
        service.registerChannel(new SMSChannel());
        service.registerChannel(new PushChannel());

        // --- Test 1: Basic Email Notification ---
        System.out.println("--- Test 1: Basic Email Notification ---");
        Notification n1 = NotificationFactory.createEmailNotification(
                "U-1", "alice@example.com", "Order Shipped",
                "Your order #1234 has been shipped!", Priority.HIGH);
        service.send(n1);
        System.out.println();

        // --- Test 2: SMS Notification ---
        System.out.println("--- Test 2: SMS Notification ---");
        Notification n2 = NotificationFactory.createSMSNotification(
                "U-1", "+1-555-0101", "Your OTP is 123456. Valid for 5 minutes.", Priority.CRITICAL);
        service.send(n2);
        System.out.println();

        // --- Test 3: Push Notification ---
        System.out.println("--- Test 3: Push Notification ---");
        Notification n3 = NotificationFactory.createPushNotification(
                "U-2", "device_abc123", "New Message", "You have a new message from Alice", Priority.MEDIUM);
        service.send(n3);
        System.out.println();

        // --- Test 4: Template-based Notification ---
        System.out.println("--- Test 4: Template-based Notification ---");
        Template orderTemplate = new Template("T-1", "Order Confirmation",
                "Hi {{name}}, your order #{{orderId}} of ${{amount}} has been confirmed. Delivery by {{date}}.");
        Map<String, String> vars = new HashMap<>();
        vars.put("name", "Bob");
        vars.put("orderId", "5678");
        vars.put("amount", "99.99");
        vars.put("date", "May 20, 2026");
        Notification n4 = NotificationFactory.fromTemplate("U-2", "bob@example.com", "EMAIL",
                orderTemplate, vars, Priority.HIGH);
        service.send(n4);
        System.out.println();

        // --- Test 5: Throttling ---
        System.out.println("--- Test 5: Throttling (limit: 3/minute for EMAIL) ---");
        // U-1 already sent 1 email, send 2 more to hit limit
        Notification n5 = NotificationFactory.createEmailNotification(
                "U-1", "alice@example.com", "Promo", "Check out our sale!", Priority.LOW);
        service.send(n5);
        Notification n6 = NotificationFactory.createEmailNotification(
                "U-1", "alice@example.com", "Reminder", "Don't forget to review!", Priority.LOW);
        service.send(n6);
        // This should be throttled
        Notification n7 = NotificationFactory.createEmailNotification(
                "U-1", "alice@example.com", "Another Promo", "More deals!", Priority.LOW);
        service.send(n7);
        System.out.println();

        // --- Test 6: Retry Decorator ---
        System.out.println("--- Test 6: Retry with Decorator (simulated failures) ---");
        RetryDecorator retryEmail = new RetryDecorator(new EmailChannel(), 3);
        retryEmail.setSimulatedFailures(2); // Fail twice, succeed on 3rd

        // Replace channel for this test
        NotificationService retryService = new NotificationService(10);
        retryService.registerChannel(retryEmail);

        Notification n8 = NotificationFactory.createEmailNotification(
                "U-3", "charlie@example.com", "Critical Alert",
                "System health warning!", Priority.CRITICAL);
        retryService.send(n8);
        System.out.println();

        // --- Test 7: Priority-based Batch ---
        System.out.println("--- Test 7: Priority-based Batch Sending ---");
        NotificationService batchService = new NotificationService(20);
        batchService.registerChannel(new EmailChannel());
        batchService.registerChannel(new SMSChannel());
        batchService.registerChannel(new PushChannel());

        List<Notification> batch = new ArrayList<>();
        batch.add(NotificationFactory.createEmailNotification("U-4", "user4@test.com",
                "Low Priority", "Newsletter content", Priority.LOW));
        batch.add(NotificationFactory.createSMSNotification("U-4", "+1-555-0404",
                "CRITICAL: Account security alert!", Priority.CRITICAL));
        batch.add(NotificationFactory.createPushNotification("U-4", "device_xyz",
                "Medium", "App update available", Priority.MEDIUM));

        System.out.println("Sending batch (sorted by priority - CRITICAL first):");
        batchService.sendBatch(batch);
        System.out.println();

        // --- Test 8: Statistics ---
        System.out.println("--- Test 8: Notification Statistics ---");
        System.out.println("Main service stats:");
        System.out.println("  Sent: " + service.getSentCount());
        System.out.println("  Failed: " + service.getFailedCount());
        System.out.println("  Throttled: " + service.getThrottledCount());
        System.out.println("  Total: " + service.getHistory().size());
        System.out.println();

        System.out.println("=== Notification System Demo Complete ===");
    }
}
