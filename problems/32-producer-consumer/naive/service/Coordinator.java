/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Coordinator.java — Manages lifecycle of producers and consumers
import java.util.ArrayList;
import java.util.List;

public class Coordinator {
    private final BoundedBuffer buffer;                            // private final = shared buffer for all actors
    private final List<Thread> producerThreads = new ArrayList<>(); // private = internal lifecycle management
    private final List<Thread> consumerThreads = new ArrayList<>();
    private final List<Producer> producers = new ArrayList<>();
    private final List<Consumer> consumers = new ArrayList<>();

    public Coordinator(BoundedBuffer buffer) {
        this.buffer = buffer;
    }

    public void addProducer(Producer producer) {
        producers.add(producer);
        Thread t = new Thread(producer, producer.getName());
        t.setDaemon(true);
        producerThreads.add(t);
    }

    public void addConsumer(Consumer consumer) {
        consumers.add(consumer);
        Thread t = new Thread(consumer, consumer.getName());
        t.setDaemon(true);
        consumerThreads.add(t);
    }

    public void start() {
        System.out.println("  [Coordinator] Starting " + producers.size() +
                " producers and " + consumers.size() + " consumers");
        for (Thread t : consumerThreads) t.start();
        for (Thread t : producerThreads) t.start();
    }

    public void awaitCompletion(long timeoutMs) throws InterruptedException {
        for (Thread t : producerThreads) t.join(timeoutMs);
        for (Thread t : consumerThreads) t.join(timeoutMs);
    }

    public void shutdown() {
        for (Producer p : producers) p.stop();
        for (Consumer c : consumers) c.stop();
        for (Thread t : producerThreads) t.interrupt();
        for (Thread t : consumerThreads) t.interrupt();
    }
}
