/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/RetryDecorator.java — Decorator adding retry logic on delivery failure

public class RetryDecorator implements Channel { // implements = fulfills the Channel interface contract
    private Channel wrappedChannel;              // private = hides the decorated channel from outside
    private int maxRetries;
    private int failureCount; // for testing: simulates N failures before success

    public RetryDecorator(Channel channel, int maxRetries) {
        this.wrappedChannel = channel;
        this.maxRetries = maxRetries;
        this.failureCount = 0;
    }

    // For testing: simulate N failures before success
    public void setSimulatedFailures(int count) {
        this.failureCount = count;
    }

    @Override                                     // @Override = ensures this method matches Channel interface
    public boolean send(Notification notification) {
        int attempts = 0;
        while (attempts <= maxRetries) {
            notification.incrementAttempts();
            attempts++;

            boolean success;
            if (failureCount > 0) {
                failureCount--;
                success = false;
                System.out.printf("  [RETRY] Attempt %d/%d for %s failed. %s%n",
                        attempts, maxRetries + 1, notification.getNotificationId(),
                        attempts <= maxRetries ? "Retrying..." : "Giving up.");
            } else {
                success = wrappedChannel.send(notification);
            }

            if (success) {
                if (attempts > 1) {
                    System.out.printf("  [RETRY] Succeeded on attempt %d for %s%n",
                            attempts, notification.getNotificationId());
                }
                return true;
            }

            if (attempts <= maxRetries) {
                System.out.printf("  [RETRY] Attempt %d/%d failed for %s. Retrying...%n",
                        attempts, maxRetries + 1, notification.getNotificationId());
            }
        }
        return false;
    }

    @Override
    public String getType() { return wrappedChannel.getType(); }
}
