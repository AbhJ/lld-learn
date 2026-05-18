/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/SocialNetwork.java — Facade orchestrating posting, following, feeds, and notifications

import java.util.*;

public class SocialNetwork {
    private Map<String, User> users;                       // private = user registry by ID
    private FollowManager followManager;                   // private = manages follow relationships
    private List<Post> allPosts;                           // private = global post store
    private Map<String, List<Notification>> notifications; // private = per-user notification inbox

    public SocialNetwork() {
        this.users = new HashMap<>();
        this.followManager = new FollowManager();
        this.allPosts = new ArrayList<>();
        this.notifications = new HashMap<>();
    }

    public void registerUser(User user) {
        users.put(user.getUserId(), user);
        notifications.put(user.getUserId(), new ArrayList<>());
    }

    public Post createPost(User user, String content) {
        Post post = user.createPost(content);
        allPosts.add(post);

        // Notify followers
        Set<String> followers = followManager.getFollowers(user.getUserId());
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
        return followManager.unfollow(follower.getUserId(), followee.getUserId());
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

    public List<Post> getFeed(User user, FeedStrategy strategy) {
        Feed feed = new Feed(user, strategy);
        Set<String> following = followManager.getFollowing(user.getUserId());
        return feed.generate(allPosts, following);
    }

    public FeedIterator getPaginatedFeed(User user, FeedStrategy strategy, int pageSize) {
        List<Post> feedPosts = getFeed(user, strategy);
        return new FeedIterator(feedPosts, pageSize);
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
