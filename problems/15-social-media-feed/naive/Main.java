/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the social media feed system

/*
 * VARIATIONS FREQUENTLY ASKED:
 * 1. Stories (24hr expiry) - Ephemeral content, view tracking, story reactions
 * 2. Live streaming - Real-time comments, viewer count, go-live notification
 * 3. Content moderation - ML-based flagging, report handling, appeals
 * 4. Hashtag/trending - Trending algorithm, hashtag following, explore page
 * 5. Reels/short video - Video upload, recommendation, engagement scoring
 *
 * See VARIATIONS.md for full solution approaches.
 */
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Social Media Feed Demo ===\n");

        SocialNetwork network = new SocialNetwork();

        // Create users
        User alice = new User("U-1", "Alice", "Software Engineer");
        User bob = new User("U-2", "Bob", "Designer");
        User charlie = new User("U-3", "Charlie", "Product Manager");
        User diana = new User("U-4", "Diana", "Data Scientist");

        network.registerUser(alice);
        network.registerUser(bob);
        network.registerUser(charlie);
        network.registerUser(diana);

        // --- Test 1: Follow ---
        System.out.println("--- Test 1: Follow Users ---");
        network.follow(bob, alice);
        network.follow(charlie, alice);
        network.follow(bob, charlie);
        network.follow(diana, alice);
        network.follow(diana, bob);
        System.out.println("Alice followers: " + network.getFollowerCount(alice));
        System.out.println("Bob following: " + network.getFollowingCount(bob));
        System.out.println();

        // --- Test 2: Create Posts ---
        System.out.println("--- Test 2: Create Posts ---");
        Post p1 = network.createPost(alice, "Hello World! My first post!");
        Post p2 = network.createPost(alice, "Just shipped a new feature!");
        Post p3 = network.createPost(charlie, "Product roadmap for Q3 is ready");
        Post p4 = network.createPost(bob, "New design system launched");
        Post p5 = network.createPost(alice, "Weekend hiking adventure");
        System.out.println("Posts created: 5");
        System.out.println();

        // --- Test 3: Likes and Comments ---
        System.out.println("--- Test 3: Likes and Comments ---");
        network.likePost(bob, p1);
        network.likePost(charlie, p1);
        network.likePost(diana, p1);
        network.likePost(bob, p2);
        network.likePost(diana, p2);
        network.likePost(diana, p5);

        network.commentOnPost(bob, p1, "Welcome to the platform!");
        network.commentOnPost(charlie, p1, "Great to see you here!");
        network.commentOnPost(diana, p2, "Awesome work!");

        System.out.println("Post 1: " + p1);
        System.out.println("Post 2: " + p2);
        System.out.println("Post 3: " + p3);
        System.out.println();

        // --- Test 4: Chronological Feed ---
        System.out.println("--- Test 4: Bob's Feed (Chronological) ---");
        List<Post> chronFeed = network.getFeed(bob, new ChronologicalFeed());
        for (Post post : chronFeed) {
            System.out.println("  " + post);
        }
        System.out.println();

        // --- Test 5: Ranked Feed ---
        System.out.println("--- Test 5: Bob's Feed (Engagement-Ranked) ---");
        List<Post> rankedFeed = network.getFeed(bob, new RankedFeed());
        for (Post post : rankedFeed) {
            System.out.println("  " + post + " [engagement: " + post.getEngagement() + "]");
        }
        System.out.println();

        // --- Test 6: Paginated Feed ---
        System.out.println("--- Test 6: Diana's Paginated Feed (page size=2) ---");
        FeedIterator iterator = network.getPaginatedFeed(diana, new ChronologicalFeed(), 2);
        int page = 1;
        while (iterator.hasNextPage()) {
            System.out.println("  Page " + page + ":");
            List<Post> pagePosts = iterator.nextPage();
            for (Post post : pagePosts) {
                System.out.println("    " + post);
            }
            page++;
        }
        System.out.println("  Total pages: " + iterator.getTotalPages());
        System.out.println();

        // --- Test 7: Notifications ---
        System.out.println("--- Test 7: Alice's Notifications ---");
        List<Notification> aliceNotifs = network.getNotifications(alice);
        for (Notification n : aliceNotifs) {
            System.out.println("  " + n);
        }
        System.out.println("  Unread: " + network.getUnreadNotifications(alice).size());
        System.out.println();

        // --- Test 8: Unfollow ---
        System.out.println("--- Test 8: Unfollow ---");
        System.out.println("Bob's feed size before unfollow: " + network.getFeed(bob, new ChronologicalFeed()).size());
        network.unfollow(bob, alice);
        System.out.println("Bob unfollowed Alice.");
        System.out.println("Bob's feed size after unfollow: " + network.getFeed(bob, new ChronologicalFeed()).size());
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
