/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 5 users posting while 10 readers generate feeds, no duplicates/missing

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Social Media Feed Demo ===\n");

        FeedService service = new FeedService();

        // Create 5 users, each follows all others
        int userCount = 5;
        for (int i = 0; i < userCount; i++) {
            service.registerUser(new User("U-" + i, "User-" + i));
        }
        for (int i = 0; i < userCount; i++) {
            for (int j = 0; j < userCount; j++) {
                if (i != j) {
                    service.follow("U-" + i, "U-" + j);
                }
            }
        }

        int postsPerUser = 20;
        int readerCount = 10;
        int totalExpectedPosts = userCount * postsPerUser;

        System.out.println("Scenario: " + userCount + " users each posting " + postsPerUser +
                " messages simultaneously,");
        System.out.println("          while " + readerCount + " readers generate feeds concurrently.");
        System.out.println("Expected: No duplicates, no ConcurrentModificationException,");
        System.out.println("          all " + totalExpectedPosts + " posts eventually visible.\n");

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch writersDone = new CountDownLatch(userCount);
        CountDownLatch readersDone = new CountDownLatch(readerCount);
        AtomicInteger postsCreated = new AtomicInteger(0);
        AtomicInteger feedsGenerated = new AtomicInteger(0);
        AtomicInteger errors = new AtomicInteger(0);
        List<String> readerResults = Collections.synchronizedList(new ArrayList<>());

        // Writer threads — each user posts messages
        for (int i = 0; i < userCount; i++) {
            final int userId = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int p = 0; p < postsPerUser; p++) {
                        Post post = service.createPost("U-" + userId,
                                "Post-" + p + " from User-" + userId);
                        if (post != null) {
                            postsCreated.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    writersDone.countDown();
                }
            }, "Writer-" + i).start();
        }

        // Reader threads — generate feeds while posts are being created
        for (int i = 0; i < readerCount; i++) {
            final int readerId = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    String userId = "U-" + (readerId % userCount);

                    // Generate feed multiple times during writing
                    for (int r = 0; r < 5; r++) {
                        List<Post> feed = service.generateFeed(userId);
                        feedsGenerated.incrementAndGet();

                        // Check for duplicates in this feed
                        Set<Long> postIds = new HashSet<>();
                        for (Post post : feed) {
                            if (!postIds.add(post.getPostId())) {
                                errors.incrementAndGet();
                                readerResults.add("  [ERROR] Duplicate post in feed: " + post.getPostId());
                            }
                        }
                        Thread.sleep(1); // small delay to interleave with writers
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ConcurrentModificationException e) {
                    errors.incrementAndGet();
                    readerResults.add("  [ERROR] ConcurrentModificationException in reader-" + readerId);
                } catch (Exception e) {
                    errors.incrementAndGet();
                    readerResults.add("  [ERROR] " + e.getClass().getSimpleName() + " in reader-" + readerId);
                } finally {
                    readersDone.countDown();
                }
            }, "Reader-" + i).start();
        }

        startLatch.countDown();
        writersDone.await();
        readersDone.await();

        // Print any errors
        if (!readerResults.isEmpty()) {
            for (String r : readerResults) {
                System.out.println(r);
            }
        } else {
            System.out.println("  No errors detected during concurrent read/write operations.");
        }

        // Final verification — generate feed after all writes complete
        System.out.println("\nFinal feed verification (after all posts complete):");
        List<Post> finalFeed = service.generateFeed("U-0");
        // U-0 follows U-1..U-4, so should see (userCount-1) * postsPerUser posts
        int expectedInFeed = (userCount - 1) * postsPerUser;
        Set<Long> uniquePostIds = new HashSet<>();
        for (Post post : finalFeed) {
            uniquePostIds.add(post.getPostId());
        }
        boolean noDuplicates = uniquePostIds.size() == finalFeed.size();
        boolean allPostsVisible = finalFeed.size() == expectedInFeed;

        System.out.println("  Feed size for U-0: " + finalFeed.size() + " (expected " + expectedInFeed + ")");
        System.out.println("  Unique posts: " + uniquePostIds.size());

        // User post counts
        System.out.println("\nUser post counts:");
        for (int i = 0; i < userCount; i++) {
            User u = service.getUser("U-" + i);
            System.out.println("  " + u);
        }

        // Summary
        System.out.println("\n--- Summary ---");
        System.out.println("Total posts created: " + postsCreated.get() + " (expected " + totalExpectedPosts + ")");
        System.out.println("Feeds generated: " + feedsGenerated.get());
        System.out.println("Errors: " + errors.get());

        boolean correctPostCount = postsCreated.get() == totalExpectedPosts;

        System.out.println("\nAll posts created: " + (correctPostCount ? "PASSED" : "FAILED"));
        System.out.println("No duplicates in feed: " + (noDuplicates ? "PASSED" : "FAILED"));
        System.out.println("All posts visible in final feed: " + (allPostsVisible ? "PASSED" : "FAILED"));
        System.out.println("No concurrent errors: " + (errors.get() == 0 ? "PASSED" : "FAILED"));

        boolean allPassed = correctPostCount && noDuplicates && allPostsVisible && errors.get() == 0;
        System.out.println("\nOverall: " + (allPassed ? "ALL TESTS PASSED" : "SOME TESTS FAILED"));
    }
}
