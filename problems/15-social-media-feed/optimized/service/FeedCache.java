/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/FeedCache.java — Pre-computed feed with fan-out-on-write and pagination cursor

import java.util.*;

/**
 * Optimized feed: uses fan-out-on-write to pre-compute user feeds.
 * When a post is created, it's pushed to all followers' feed lists immediately.
 * Reading a feed is O(1) (just read from the pre-computed list).
 * Naive approach rebuilds the feed on every request by scanning all posts.
 */
public class FeedCache {
    private Map<String, LinkedList<Post>> userFeeds; // HashMap+LinkedList = O(1) lookup + O(1) addFirst/removeLast
    private int maxFeedSize;                         // caps memory usage; evicts oldest on overflow

    public FeedCache(int maxFeedSize) {
        this.userFeeds = new HashMap<>();
        this.maxFeedSize = maxFeedSize;
    }

    /**
     * Fan-out-on-write: push post to all followers' feeds.
     * O(f) where f = number of followers.
     */
    public void fanOutPost(Post post, Set<String> followers) {
        for (String followerId : followers) {
            LinkedList<Post> feed = userFeeds.computeIfAbsent(followerId, k -> new LinkedList<>());
            feed.addFirst(post); // newest first
            if (feed.size() > maxFeedSize) {
                feed.removeLast(); // evict oldest
            }
        }
    }

    /**
     * Get feed with cursor-based pagination.
     * Returns posts after the cursor position. O(pageSize).
     */
    public List<Post> getFeed(String userId, int cursor, int pageSize) {
        LinkedList<Post> feed = userFeeds.get(userId);
        if (feed == null || cursor >= feed.size()) return Collections.emptyList();

        int end = Math.min(cursor + pageSize, feed.size());
        return new ArrayList<>(feed.subList(cursor, end));
    }

    /**
     * Get full feed for ranking (used when strategy needs all posts).
     */
    public List<Post> getFullFeed(String userId) {
        LinkedList<Post> feed = userFeeds.get(userId);
        return feed != null ? new ArrayList<>(feed) : Collections.emptyList();
    }

    /**
     * Remove posts by a user from all feeds (on unfollow).
     */
    public void removeAuthorFromFeed(String feedOwnerId, String authorId) {
        LinkedList<Post> feed = userFeeds.get(feedOwnerId);
        if (feed != null) {
            feed.removeIf(p -> p.getAuthorId().equals(authorId));
        }
    }

    public int getFeedSize(String userId) {
        LinkedList<Post> feed = userFeeds.get(userId);
        return feed != null ? feed.size() : 0;
    }
}
