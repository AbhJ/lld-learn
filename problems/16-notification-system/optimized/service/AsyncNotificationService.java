/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/AsyncNotificationService.java — Async delivery with BlockingQueue per channel and batch processing

import java.util.*;
import java.util.concurrent.*;

/**
 * Optimized: Asynchronous notification delivery using BlockingQueue per channel.
 * Each channel has its own delivery thread, preventing slow channels from blocking others.
 * Supports batch processing and per-user rate limiting.
 */
public class AsyncNotificationService {
    private Map<String, Channel> channels;
    private Map<String, BlockingQueue<Notification>> channelQueues; // BlockingQueue = thread-safe queue; worker blocks until item arrives
    private Map<String, Thread> channelWorkers;
    private RateLimiter rateLimiter;
    private List<Notification> history;          // synchronizedList wraps this for thread-safe writes
    private volatile boolean running;            // volatile = visible to worker threads immediately on change

    public AsyncNotificationService(int rateLimit) {
        this.channels = new HashMap<>();
        this.channelQueues = new HashMap<>();
        this.channelWorkers = new HashMap<>();
        this.rateLimiter = new RateLimiter(rateLimit);
        this.history = Collections.synchronizedList(new ArrayList<>());
        this.running = true;
    }

    public void registerChannel(Channel channel) {
        channels.put(channel.getType(), channel);
        BlockingQueue<Notification> queue = new LinkedBlockingQueue<>(1000); // LinkedBlockingQueue = bounded buffer; prevents memory overflow
        channelQueues.put(channel.getType(), queue);

        // Start async worker thread for this channel
        Thread worker = new Thread(() -> processQueue(channel, queue), "Channel-" + channel.getType());
        worker.setDaemon(true);
        worker.start();
        channelWorkers.put(channel.getType(), worker);
    }

    private void processQueue(Channel channel, BlockingQueue<Notification> queue) {
        List<Notification> batch = new ArrayList<>();
        while (running) {
            try {
                // Wait for at least one notification
                Notification first = queue.poll(100, TimeUnit.MILLISECONDS);
                if (first == null) continue;

                batch.clear();
                batch.add(first);
                // Drain up to 10 more for batch processing
                queue.drainTo(batch, 10);

                // Process batch sorted by priority
                batch.sort((a, b) -> b.getPriority().getLevel() - a.getPriority().getLevel());
                for (Notification n : batch) {
                    boolean success = channel.send(n);
                    n.setStatus(success ? Notification.Status.SENT : Notification.Status.FAILED);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Non-blocking send: enqueues notification for async delivery.
     * Returns immediately instead of blocking on channel I/O.
     */
    public boolean send(Notification notification) {
        history.add(notification);

        // Rate limit check
        if (!rateLimiter.allowSend(notification.getUserId(), notification.getChannelType())) {
            notification.setStatus(Notification.Status.THROTTLED);
            System.out.printf("  [THROTTLED] Rate limit exceeded for user %s on %s%n",
                    notification.getUserId(), notification.getChannelType());
            return false;
        }

        // Enqueue for async delivery
        BlockingQueue<Notification> queue = channelQueues.get(notification.getChannelType());
        if (queue == null) {
            notification.setStatus(Notification.Status.FAILED);
            System.out.println("  [ERROR] No channel registered for: " + notification.getChannelType());
            return false;
        }

        boolean offered = queue.offer(notification);
        if (!offered) {
            notification.setStatus(Notification.Status.FAILED);
            System.out.println("  [ERROR] Channel queue full for: " + notification.getChannelType());
            return false;
        }
        return true;
    }

    /**
     * Synchronous send (for testing/demo where we want immediate output).
     */
    public boolean sendSync(Notification notification) {
        history.add(notification);

        if (!rateLimiter.allowSend(notification.getUserId(), notification.getChannelType())) {
            notification.setStatus(Notification.Status.THROTTLED);
            System.out.printf("  [THROTTLED] Rate limit exceeded for user %s on %s%n",
                    notification.getUserId(), notification.getChannelType());
            return false;
        }

        Channel channel = channels.get(notification.getChannelType());
        if (channel == null) {
            notification.setStatus(Notification.Status.FAILED);
            return false;
        }

        boolean success = channel.send(notification);
        notification.setStatus(success ? Notification.Status.SENT : Notification.Status.FAILED);
        return success;
    }

    public void sendBatch(List<Notification> notifications) {
        notifications.sort((a, b) -> b.getPriority().getLevel() - a.getPriority().getLevel());
        for (Notification n : notifications) {
            sendSync(n);
        }
    }

    public void shutdown() {
        running = false;
        for (Thread worker : channelWorkers.values()) {
            worker.interrupt();
        }
    }

    public List<Notification> getHistory() { return Collections.unmodifiableList(history); }

    public long getSentCount() {
        return history.stream().filter(n -> n.getStatus() == Notification.Status.SENT).count();
    }
    public long getFailedCount() {
        return history.stream().filter(n -> n.getStatus() == Notification.Status.FAILED).count();
    }
    public long getThrottledCount() {
        return history.stream().filter(n -> n.getStatus() == Notification.Status.THROTTLED).count();
    }
}
