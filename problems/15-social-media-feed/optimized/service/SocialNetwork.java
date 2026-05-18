/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/SocialNetwork.java — Optimized social network with fan-out-on-write feed pre-computation

import java.util.*;

public class SocialNetwork {
    private Map<String, User> users;                       // HashMap = O(1) user lookup by ID
    private FollowManager followManager;                   // manages bidirectional follow graph
    private List<Post> allPosts;                           // ArrayList = O(1) append; full post archive
    private Map<String, List<Notification>> notifications; // per-user notification inbox
    private FeedCache feedCache;                           // pre-computed feeds via fan-out-on-write

    public SocialNetwork() {
        this.users = new HashMap<>();
        this.followManager = new FollowManager();
        this.allPosts = new ArrayList<>();
        this.notifications = new HashMap<>();
        this.feedCache = new FeedCache(1000);
    }

    public void registerUser(User user) {
        users.put(user.getUserId(), user);
        notifications.put(user.getUserId(), new ArrayList<>());
    }

    /**
     * Fan-out-on-write: immediately pushes post to all followers' pre-computed feeds.
     */
    public Post createPost(User user, String content) {
        Post post = user.createPost(content);
        allPosts.add(post);

        Set<String> followers = followManager.getFollowers(user.getUserId());
        feedCache.fanOutPost(post, followers);

        for (String followerId : followers) {
            Notification notif = new Notification(followerId, user.getUserId(), user.getName(),
                    Notification.Type.NEW_POST, user.getName() + " posted: \"" + content + "\"");
            notifications.get(followerId).add(notif);
        }
        return post;
    }

    public boolean follow(User follower, User followee) {
        boolean result = followManager.follow(follower.getUserId(), followee.getUserId());
        if (result) {
            Notification notif = new Notification(followee.getUserId(), follower.getUserId(),
                    follower.getName(), Notification.Type.FOLLOW,
                    follower.getName() + " started following you");
            notifications.get(followee.getUserId()).add(notif);
        }
        return result;
    }

    public boolean unfollow(User follower, User followee) {
        boolean result = followManager.unfollow(follower.getUserId(), followee.getUserId());
        if (result) {
            feedCache.removeAuthorFromFeed(follower.getUserId(), followee.getUserId());
        }
        return result;
    }

    public boolean likePost(User user, Post post) {
        boolean liked = post.like(user.getUserId());
        if (liked && !user.getUserId().equals(post.getAuthorId())) {
            Notification notif = new Notification(post.getAuthorId(), user.getUserId(), user.getName(),
                    Notification.Type.LIKE, user.getName() + " liked your post");
            notifications.get(post.getAuthorId()).add(notif);
        }
        return liked;
    }

    public Comment commentOnPost(User user, Post post, String content) {
        Comment comment = new Comment(user.getUserId(), user.getName(), content);
        post.addComment(comment);
        if (!user.getUserId().equals(post.getAuthorId())) {
            Notification notif = new Notification(post.getAuthorId(), user.getUserId(), user.getName(),
                    Notification.Type.COMMENT, user.getName() + " commented on your post: \"" + content + "\"");
            notifications.get(post.getAuthorId()).add(notif);
        }
        return comment;
    }

    /**
     * O(1) feed retrieval from pre-computed cache + optional ranking.
     */
    public List<Post> getFeed(User user, FeedStrategy strategy) {
        List<Post> feedPosts = feedCache.getFullFeed(user.getUserId());
        return strategy.rankPosts(feedPosts);
    }

    /**
     * Cursor-based pagination: O(pageSize) retrieval.
     */
    public List<Post> getFeedPage(User user, int cursor, int pageSize) {
        return feedCache.getFeed(user.getUserId(), cursor, pageSize);
    }

    public List<Notification> getNotifications(User user) {
        return notifications.getOrDefault(user.getUserId(), Collections.emptyList());
    }

    public List<Notification> getUnreadNotifications(User user) {
        List<Notification> unread = new ArrayList<>();
        for (Notification n : getNotifications(user)) {
            if (!n.isRead()) unread.add(n);
        }
        return unread;
    }

    public int getFollowerCount(User user) { return followManager.getFollowerCount(user.getUserId()); }
    public int getFollowingCount(User user) { return followManager.getFollowingCount(user.getUserId()); }
}
