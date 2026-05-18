/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates producer-consumer with wait/notify bounded buffer
public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Producer-Consumer (Naive) Demo ===\n");

        // --- Test 1: Single Producer/Consumer ---
        System.out.println("--- Test 1: Single Producer/Consumer ---");
        BoundedBuffer buffer1 = new BoundedBuffer(5);
        Coordinator coord1 = new Coordinator(buffer1);
        coord1.addProducer(new Producer("Producer-1", buffer1, 5));
        coord1.addConsumer(new Consumer("Consumer-1", buffer1, 5));
        coord1.start();
        coord1.awaitCompletion(5000);

        // --- Test 2: Multiple Producers and Consumers ---
        System.out.println("\n--- Test 2: Multiple Producers and Consumers ---");
        BoundedBuffer buffer2 = new BoundedBuffer(3);
        Coordinator coord2 = new Coordinator(buffer2);
        coord2.addProducer(new Producer("ProducerA", buffer2, 4));
        coord2.addProducer(new Producer("ProducerB", buffer2, 4));
        coord2.addConsumer(new Consumer("ConsumerX", buffer2, 4));
        coord2.addConsumer(new Consumer("ConsumerY", buffer2, 4));
        coord2.start();
        coord2.awaitCompletion(5000);

        // --- Test 3: Backpressure ---
        System.out.println("\n--- Test 3: Backpressure (tiny buffer) ---");
        BoundedBuffer tinyBuffer = new BoundedBuffer(2);
        Coordinator coord3 = new Coordinator(tinyBuffer);
        coord3.addProducer(new Producer("FastProd", tinyBuffer, 6));
        coord3.addConsumer(new Consumer("SlowCons", tinyBuffer, 6));
        coord3.start();
        coord3.awaitCompletion(5000);

        System.out.println("\n=== Producer-Consumer (Naive) Demo Complete ===");
    }
}
