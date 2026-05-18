/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 10 users sending to same room simultaneously, verify all messages received in consistent order

import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Chat Application Demo ===\n");
        System.out.println("Race condition: Two users sending messages to same group chat simultaneously");
        System.out.println("— message ordering inconsistent, messages lost.\n");

        ChatRoom room = new ChatRoom("general");
        int userCount = 10;
        int messagesPerUser = 100;
        int totalExpected = userCount * messagesPerUser;

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(userCount);

        for (int u = 0; u < userCount; u++) {
            final String userId = "User-" + u;
            new Thread(() -> {
                try {
                    startLatch.await(); // all threads start simultaneously
                    for (int m = 0; m < messagesPerUser; m++) {
                        room.sendMessage(userId, "msg-" + m);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        // Fire all threads at once
        startLatch.countDown();
        doneLatch.await();

        // Verify correctness
        List<Message> ordered = room.getMessagesInOrder();
        int actualCount = ordered.size();

        // Check 1: No messages lost
        boolean noLoss = (actualCount == totalExpected);

        // Check 2: Sequence numbers are unique and contiguous (1..totalExpected)
        Set<Long> seqNums = new HashSet<>();
        for (Message msg : ordered) {
            seqNums.add(msg.getSequenceNumber());
        }
        boolean uniqueSeqs = (seqNums.size() == totalExpected);
        boolean contiguous = true;
        for (long i = 1; i <= totalExpected; i++) {
            if (!seqNums.contains(i)) {
                contiguous = false;
                break;
            }
        }

        // Check 3: Ordering is consistent (sorted by sequence)
        boolean orderConsistent = true;
        for (int i = 1; i < ordered.size(); i++) {
            if (ordered.get(i).getSequenceNumber() <= ordered.get(i - 1).getSequenceNumber()) {
                orderConsistent = false;
                break;
            }
        }

        // Check 4: Each user sent exactly messagesPerUser messages
        Map<String, Integer> perUser = new HashMap<>();
        for (Message msg : ordered) {
            perUser.merge(msg.getSenderId(), 1, Integer::sum);
        }
        boolean perUserCorrect = true;
        for (int u = 0; u < userCount; u++) {
            if (perUser.getOrDefault("User-" + u, 0) != messagesPerUser) {
                perUserCorrect = false;
                break;
            }
        }

        System.out.println("--- Results ---");
        System.out.println("Users: " + userCount);
        System.out.println("Messages per user: " + messagesPerUser);
        System.out.println("Total expected: " + totalExpected);
        System.out.println("Total received: " + actualCount);
        System.out.println("No messages lost: " + noLoss);
        System.out.println("Unique sequence numbers: " + uniqueSeqs);
        System.out.println("Contiguous sequences (1.." + totalExpected + "): " + contiguous);
        System.out.println("Order consistent: " + orderConsistent);
        System.out.println("Per-user count correct: " + perUserCorrect);

        System.out.println("\nSample messages (first 10):");
        for (int i = 0; i < Math.min(10, ordered.size()); i++) {
            System.out.println("  " + ordered.get(i));
        }

        boolean passed = noLoss && uniqueSeqs && contiguous && orderConsistent && perUserCorrect;
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}
