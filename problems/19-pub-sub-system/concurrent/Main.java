/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 5 publishers + subscribers joining/leaving simultaneously, verify delivery consistency

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Pub-Sub System Demo ===\n");

        MessageBroker broker = new MessageBroker();
        String topicName = "events";

        int publisherCount = 5;
        int messagesPerPublisher = 20;
        int subscriberCount = 5;
        int totalExpectedMessages = publisherCount * messagesPerPublisher;

        System.out.println("Scenario: " + publisherCount + " publishers sending " + messagesPerPublisher +
                " messages each,");
        System.out.println("          while " + subscriberCount +
                " subscribers join and leave simultaneously.");
        System.out.println("Expected: No ConcurrentModificationException, consistent delivery,");
        System.out.println("          all " + totalExpectedMessages + " messages recorded in topic.\n");

        // Pre-register some subscribers
        ConcurrentHashMap<String, ConcurrentLinkedQueue<Message>> receivedBySubscriber = new ConcurrentHashMap<>();
        List<Consumer<Message>> subscriberHandlers = new ArrayList<>();

        for (int i = 0; i < subscriberCount; i++) {
            final String subId = "Sub-" + i;
            ConcurrentLinkedQueue<Message> inbox = new ConcurrentLinkedQueue<>();
            receivedBySubscriber.put(subId, inbox);
            Consumer<Message> handler = inbox::offer;
            subscriberHandlers.add(handler);
            broker.subscribe(topicName, handler);
        }

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch publishersDone = new CountDownLatch(publisherCount);
        CountDownLatch subscribersDone = new CountDownLatch(subscriberCount);
        AtomicInteger errors = new AtomicInteger(0);
        AtomicInteger publishCount = new AtomicInteger(0);

        // Publisher threads
        for (int i = 0; i < publisherCount; i++) {
            final int pubId = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int m = 0; m < messagesPerPublisher; m++) {
                        broker.publish(topicName, "Pub-" + pubId + "-Msg-" + m, "Publisher-" + pubId);
                        publishCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ConcurrentModificationException e) {
                    errors.incrementAndGet();
                } finally {
                    publishersDone.countDown();
                }
            }, "Publisher-" + i).start();
        }

        // Subscriber churn threads — subscribe/unsubscribe during publishing
        for (int i = 0; i < subscriberCount; i++) {
            final int subId = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    // Simulate join/leave/rejoin pattern
                    for (int cycle = 0; cycle < 3; cycle++) {
                        String dynamicSubId = "DynSub-" + subId + "-" + cycle;
                        ConcurrentLinkedQueue<Message> inbox = new ConcurrentLinkedQueue<>();
                        receivedBySubscriber.put(dynamicSubId, inbox);
                        Consumer<Message> handler = inbox::offer;
                        broker.subscribe(topicName, handler);
                        Thread.sleep(2); // stay subscribed briefly
                        broker.unsubscribe(topicName, handler);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ConcurrentModificationException e) {
                    errors.incrementAndGet();
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    subscribersDone.countDown();
                }
            }, "SubChurn-" + i).start();
        }

        startLatch.countDown();
        publishersDone.await();
        subscribersDone.await();

        // Verify topic message history
        Topic topic = broker.getTopic(topicName);
        int topicMessageCount = topic.getMessageCount();

        // Check sequence number ordering in topic history
        long lastSeq = 0;
        boolean orderingValid = true;
        for (Message msg : topic.getMessageHistory()) {
            if (msg.getSequenceNumber() <= lastSeq) {
                // Messages from different threads may interleave, but each should have unique seq
                // We check uniqueness, not strict ordering (concurrent publishers)
            }
            lastSeq = Math.max(lastSeq, msg.getSequenceNumber());
        }

        // Check no duplicate sequence numbers
        Set<Long> seqNumbers = new HashSet<>();
        boolean noDuplicates = true;
        for (Message msg : topic.getMessageHistory()) {
            if (!seqNumbers.add(msg.getSequenceNumber())) {
                noDuplicates = false;
                break;
            }
        }

        // Check static subscribers received all messages
        boolean staticSubsComplete = true;
        for (int i = 0; i < subscriberCount; i++) {
            ConcurrentLinkedQueue<Message> inbox = receivedBySubscriber.get("Sub-" + i);
            if (inbox.size() != totalExpectedMessages) {
                staticSubsComplete = false;
            }
        }

        // Print summary
        System.out.println("Static subscriber delivery:");
        for (int i = 0; i < subscriberCount; i++) {
            ConcurrentLinkedQueue<Message> inbox = receivedBySubscriber.get("Sub-" + i);
            System.out.println("  Sub-" + i + " received: " + inbox.size() + " messages");
        }

        System.out.println("\n--- Summary ---");
        System.out.println("Publishers: " + publisherCount);
        System.out.println("Messages per publisher: " + messagesPerPublisher);
        System.out.println("Total published: " + publishCount.get());
        System.out.println("Topic message history size: " + topicMessageCount);
        System.out.println("Unique sequence numbers: " + seqNumbers.size());
        System.out.println("Concurrent errors: " + errors.get());

        boolean allPublished = publishCount.get() == totalExpectedMessages;
        boolean historyComplete = topicMessageCount == totalExpectedMessages;

        System.out.println("\nAll messages published: " + (allPublished ? "PASSED" : "FAILED"));
        System.out.println("Topic history complete: " + (historyComplete ? "PASSED" : "FAILED"));
        System.out.println("No duplicate sequence numbers: " + (noDuplicates ? "PASSED" : "FAILED"));
        System.out.println("Static subscribers got all messages: " + (staticSubsComplete ? "PASSED" : "FAILED"));
        System.out.println("No concurrent errors: " + (errors.get() == 0 ? "PASSED" : "FAILED"));

        boolean allPassed = allPublished && historyComplete && noDuplicates && staticSubsComplete && errors.get() == 0;
        System.out.println("\nOverall: " + (allPassed ? "ALL TESTS PASSED" : "SOME TESTS FAILED"));
    }
}
