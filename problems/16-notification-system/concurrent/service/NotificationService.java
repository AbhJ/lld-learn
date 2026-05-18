/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/NotificationService.java — Thread-safe notification delivery with throttling

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe notification service with per-user rate limiting.
 *
 * Race condition solved: Multiple events triggering notifications to same user.
 * Without atomic throttle, all threads could read count < limit before any increments,
 * bypassing the rate limit. AtomicInteger with CAS ensures exact limit enforcement.
 */
class NotificationService {
    private final Throttler throttler;           // final = reference never changes; safe publication to threads
    private final ConcurrentLinkedQueue<Notification> delivered = new ConcurrentLinkedQueue<>(); // ConcurrentLinkedQueue = lock-free thread-safe queue
    private final ConcurrentLinkedQueue<Notification> throttled = new ConcurrentLinkedQueue<>(); // lock-free queue; multiple threads can offer simultaneously
    private final AtomicInteger deliveredCount = new AtomicInteger(0); // AtomicInteger = thread-safe counter without locks
    private final AtomicInteger throttledCount = new AtomicInteger(0); // AtomicInteger = uses CAS for atomic increment

    public NotificationService(Throttler throttler) {
        this.throttler = throttler;
    }

    /**
     * Send a notification. Respects per-user rate limit.
     * Thread-safe: throttler uses CAS to count permits.
     */
    public boolean send(Notification notification) {
        if (throttler.tryAcquire(notification.getUserId())) {
            notification.markDelivered();
            delivered.offer(notification);
            deliveredCount.incrementAndGet();
            return true;
        } else {
            throttled.offer(notification);
            throttledCount.incrementAndGet();
            return false;
        }
    }

    public int getDeliveredCount() { return deliveredCount.get(); }
    public int getThrottledCount() { return throttledCount.get(); }
    public ConcurrentLinkedQueue<Notification> getDelivered() { return delivered; }
    public ConcurrentLinkedQueue<Notification> getThrottled() { return throttled; }
}
