/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating optimized feed with fan-out-on-write and cursor pagination

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Social Media Feed (Optimized) ===\n");

        SocialNetwork network = new SocialNetwork();

        User alice = new User("U-1", "Alice", "Software Engineer");
        User bob = new User("U-2", "Bob", "Designer");
        User charlie = new User("U-3", "Charlie", "Product Manager");
        User diana = new User("U-4", "Diana", "Data Scientist");

        network.registerUser(alice);
        network.registerUser(bob);
        network.registerUser(charlie);
        network.registerUser(diana);

        // --- Test 1: Follow (builds fan-out targets) ---
        System.out.println("--- Test 1: Follow Users ---");
        network.follow(bob, alice);
        network.follow(charlie, alice);
        network.follow(bob, charlie);
        network.follow(diana, alice);
        network.follow(diana, bob);
        System.out.println("Alice followers: " + network.getFollowerCount(alice));
        System.out.println();

        // --- Test 2: Posts (fan-out-on-write to followers) ---
        System.out.println("--- Test 2: Create Posts (fan-out-on-write) ---");
        Post p1 = network.createPost(alice, "Hello World! My first post!");
        Post p2 = network.createPost(alice, "Just shipped a new feature!");
        Post p3 = network.createPost(charlie, "Product roadmap for Q3 is ready");
        Post p4 = network.createPost(bob, "New design system launched");
        Post p5 = network.createPost(alice, "Weekend hiking adventure");
        System.out.println("Posts created and fanned out to followers.\n");

        // --- Test 3: Engagement ---
        System.out.println("--- Test 3: Likes and Comments ---");
        network.likePost(bob, p1);
        network.likePost(charlie, p1);
        network.likePost(diana, p1);
        network.likePost(bob, p2);
        network.commentOnPost(bob, p1, "Welcome to the platform!");
        network.commentOnPost(diana, p2, "Awesome work!");
        System.out.println("Engagement added.\n");

        // --- Test 4: Pre-computed Feed (O(1) retrieval) ---
        System.out.println("--- Test 4: Bob's Feed (pre-computed, chronological) ---");
        List<Post> feed = network.getFeed(bob, new ChronologicalFeed());
        for (Post post : feed) {
            System.out.println("  " + post);
        }
        System.out.println();

        // --- Test 5: Ranked Feed ---
        System.out.println("--- Test 5: Bob's Feed (engagement-ranked) ---");
        List<Post> ranked = network.getFeed(bob, new RankedFeed());
        for (Post post : ranked) {
            System.out.println("  " + post + " [engagement: " + post.getEngagement() + "]");
        }
        System.out.println();

        // --- Test 6: Cursor Pagination ---
        System.out.println("--- Test 6: Diana's Feed (cursor pagination, page=2) ---");
        int cursor = 0;
        int pageSize = 2;
        int page = 1;
        while (true) {
            List<Post> feedPage = network.getFeedPage(diana, cursor, pageSize);
            if (feedPage.isEmpty()) break;
            System.out.println("  Page " + page + ":");
            for (Post post : feedPage) {
                System.out.println("    " + post);
            }
            cursor += pageSize;
            page++;
        }
        System.out.println();

        // --- Test 7: Unfollow removes from feed ---
        System.out.println("--- Test 7: Unfollow Removes From Feed ---");
        System.out.println("Bob feed size before unfollow: " + network.getFeed(bob, new ChronologicalFeed()).size());
        network.unfollow(bob, alice);
        System.out.println("Bob feed size after unfollow Alice: " + network.getFeed(bob, new ChronologicalFeed()).size());
        System.out.println();

        // === Test: Decorator pattern — stack post enrichments ===
        System.out.println("--- Test: Post Decorator ---");
        Post basePost = new Post(alice.getUserId(), alice.getName(), "Just shipped a feature!");
        PostComponent decorated = new MetadataDecorator(
                new TrendingBadgeDecorator(
                        new VerifiedBadgeDecorator(basePost)),
                12345);
        System.out.println("  Plain:     " + basePost.getDisplayContent());
        System.out.println("  Decorated: " + decorated.getDisplayContent());
        System.out.println();

        System.out.println("=== Social Media Feed Demo Complete ===");
    }
}
