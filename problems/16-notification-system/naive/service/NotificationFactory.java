/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/NotificationFactory.java — Factory creating notifications for different channels
// DESIGN PATTERN: Factory

import java.util.Map;

public class NotificationFactory {

    public static Notification createEmailNotification(String userId, String email, // static = no instance needed; called as NotificationFactory.create...()
                                                        String subject, String body, Priority priority) {
        return new Notification(userId, email, "EMAIL", subject, body, priority);
    }

    public static Notification createSMSNotification(String userId, String phone,
                                                      String body, Priority priority) {
        return new Notification(userId, phone, "SMS", "SMS", body, priority);
    }

    public static Notification createPushNotification(String userId, String deviceId,
                                                       String title, String body, Priority priority) {
        return new Notification(userId, deviceId, "PUSH", title, body, priority);
    }

    public static Notification fromTemplate(String userId, String recipient, String channelType,
                                             Template template, Map<String, String> vars, Priority priority) {
        String body = template.render(vars);
        String subject = template.getName();
        return new Notification(userId, recipient, channelType, subject, body, priority);
    }
}
