/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/EventBus.java — Per-type CopyOnWriteArrayList handlers with async CompletableFuture dispatch
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class EventBus {
    // WHY ConcurrentHashMap of CopyOnWriteArrayLists: Subscribe is rare, publish is frequent.
    // CopyOnWrite gives lock-free iteration during dispatch — no synchronization on hot path.
    private final Map<Class<?>, CopyOnWriteArrayList<HandlerEntry>> handlersByType = new ConcurrentHashMap<>(); // ConcurrentHashMap = lock-free reads; CopyOnWriteArrayList = snapshot iteration

    // WHY dedicated executor: Async dispatch doesn't block the publisher thread,
    // preventing slow handlers from cascading delays to other subscribers.
    private final Executor asyncExecutor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            r -> { Thread t = new Thread(r, "eventbus-async"); t.setDaemon(true); return t; });

    private final CopyOnWriteArrayList<Event> deadLetters = new CopyOnWriteArrayList<>(); // CopyOnWrite = safe to read concurrently
    private boolean asyncMode = false;

    @SuppressWarnings("unchecked")
    public <T extends Event> void subscribe(Class<T> eventType, EventHandler<T> handler, int priority) {
        // WHY per-type list: O(1) lookup for event type instead of scanning all subscriptions
        handlersByType.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(new HandlerEntry(handler, priority));
    }

    public <T extends Event> void subscribe(Class<T> eventType, EventHandler<T> handler) {
        subscribe(eventType, handler, 0);
    }

    public void unsubscribe(EventHandler<?> handler) {
        for (CopyOnWriteArrayList<HandlerEntry> entries : handlersByType.values()) {
            entries.removeIf(e -> e.handler == handler);
        }
    }

    @SuppressWarnings("unchecked")
    public void publish(Event event) {
        List<HandlerEntry> handlers = findHandlers(event.getClass());

        if (handlers.isEmpty()) {
            deadLetters.add(event);
            return;
        }

        if (asyncMode) {
            // WHY CompletableFuture: Non-blocking dispatch, each handler runs independently.
            // Failures in one handler don't affect others.
            for (HandlerEntry entry : handlers) {
                CompletableFuture.runAsync(() -> entry.handler.handle(event), asyncExecutor)
                        .exceptionally(ex -> {
                            System.err.println("  [EventBus] Handler error: " + ex.getMessage());
                            return null;
                        });
            }
        } else {
            for (HandlerEntry entry : handlers) {
                entry.handler.handle(event);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<HandlerEntry> findHandlers(Class<?> eventType) {
        // WHY walk class hierarchy: Allows subscribing to base Event type
        // and receiving all subtypes (UserEvent, SystemEvent, etc.)
        CopyOnWriteArrayList<HandlerEntry> result = new CopyOnWriteArrayList<>();
        Class<?> current = eventType;
        while (current != null && Event.class.isAssignableFrom(current)) {
            CopyOnWriteArrayList<HandlerEntry> entries = handlersByType.get(current);
            if (entries != null) result.addAll(entries);
            current = current.getSuperclass();
        }
        result.sort((a, b) -> Integer.compare(b.priority, a.priority));
        return result;
    }

    public void setAsyncMode(boolean async) { this.asyncMode = async; }
    public List<Event> getDeadLetters() { return deadLetters; }
    public int getSubscriptionCount() {
        int count = 0;
        for (var list : handlersByType.values()) count += list.size();
        return count;
    }

    private static class HandlerEntry { // private static = inner class with no reference to outer EventBus instance
        final EventHandler handler;    // final = never reassigned after construction
        final int priority;
        HandlerEntry(EventHandler handler, int priority) {
            this.handler = handler;
            this.priority = priority;
        }
    }
}
