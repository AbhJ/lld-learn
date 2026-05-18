/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/Channel.java — Delivery channel interface (Email, SMS, Push) implementations

public interface Channel {                       // interface = contract for delivery channel implementations
    boolean send(Notification notification);
    String getType();
}

class EmailChannel implements Channel {
    private boolean simulateFailure;

    public EmailChannel() { this.simulateFailure = false; }
    public void setSimulateFailure(boolean fail) { this.simulateFailure = fail; }

    @Override
    public boolean send(Notification notification) {
        if (simulateFailure) {
            System.out.printf("  [EMAIL] Failed to send to %s: Connection timeout%n", notification.getRecipient());
            return false;
        }
        System.out.printf("  [EMAIL] Sent to %s | Subject: %s | Body: %s%n",
                notification.getRecipient(), notification.getSubject(), notification.getBody());
        return true;
    }

    @Override
    public String getType() { return "EMAIL"; }
}

class SMSChannel implements Channel {
    @Override
    public boolean send(Notification notification) {
        System.out.printf("  [SMS] Sent to %s: %s%n",
                notification.getRecipient(), notification.getBody());
        return true;
    }

    @Override
    public String getType() { return "SMS"; }
}

class PushChannel implements Channel {
    @Override
    public boolean send(Notification notification) {
        System.out.printf("  [PUSH] Sent to device %s | Title: %s | Body: %s%n",
                notification.getRecipient(), notification.getSubject(), notification.getBody());
        return true;
    }

    @Override
    public String getType() { return "PUSH"; }
}
