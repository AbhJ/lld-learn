/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/FeedService.java — Thread-safe feed generation with CopyOnWriteArrayList + ConcurrentLinkedDeque

import java.util.*;
import java.util.concurrent.*;

/**
 * Thread-safe feed service.
 *
 * Race condition solved: User posts while another user's feed is being generated.
 * CopyOnWriteArrayList for followers ensures safe iteration during add/remove.
 * ConcurrentLinkedDeque for posts ensures safe concurrent append + read.
 * No duplicates or missing posts in generated feeds.
 */
class FeedService {
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>(); // ConcurrentHashMap = thread-safe user registry; no global lock

    public void registerUser(User user) {
        users.put(user.getUserId(), user);
    }

    public void follow(String followerId, String targetId) {
        User follower = users.get(followerId);
        User target = users.get(targetId);
        if (follower != null && target != null) {
            target.addFollower(followerId);
            follower.addFollowing(targetId);
        }
    }

    public void unfollow(String followerId, String targetId) {
        User follower = users.get(followerId);
        User target = users.get(targetId);
        if (follower != null && target != null) {
            target.removeFollower(followerId);
            follower.removeFollowing(targetId);
        }
    }

    /**
     * Create a post for a user. Thread-safe via ConcurrentLinkedDeque.
     */
    public Post createPost(String userId, String content) {
        User user = users.get(userId);
        if (user == null) return null;
        Post post = new Post(userId, content);
        user.addPost(post);
        return post;
    }

    /**
     * Generate feed for a user — collects posts from all followed users.
     * Safe to call concurrently with createPost and follow/unfollow.
     * CopyOnWriteArrayList iteration is snapshot-based (no ConcurrentModificationException).
     * ConcurrentLinkedDeque iteration is weakly consistent (sees all completed posts).
     */
    public List<Post> generateFeed(String userId) {
        User user = users.get(userId);
        if (user == null) return Collections.emptyList();

        List<Post> feed = new ArrayList<>();

        // Safe iteration over following list (snapshot)
        for (String followedId : user.getFollowing()) {
            User followed = users.get(followedId);
            if (followed != null) {
                // Safe iteration over posts (weakly consistent)
                for (Post post : followed.getPosts()) {
                    feed.add(post);
                }
            }
        }

        // Sort by timestamp (newest first)
        feed.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        return feed;
    }

    public User getUser(String userId) { return users.get(userId); }
    public int getUserCount() { return users.size(); }
}
