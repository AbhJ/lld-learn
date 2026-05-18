/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/User.java — User with thread-safe follower list and post feed

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;

class User {
    private final String userId;     // final = immutable identity; safe to read from any thread
    private final String name;       // final = never changes; safe publication guaranteed
    private final CopyOnWriteArrayList<String> followers = new CopyOnWriteArrayList<>(); // CopyOnWriteArrayList = snapshot iteration; safe during concurrent follow/unfollow
    private final CopyOnWriteArrayList<String> following = new CopyOnWriteArrayList<>(); // CopyOnWriteArrayList = addIfAbsent prevents duplicates atomically
    private final ConcurrentLinkedDeque<Post> posts = new ConcurrentLinkedDeque<>();     // ConcurrentLinkedDeque = lock-free; safe concurrent addFirst + iteration

    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public void addFollower(String followerId) {
        followers.addIfAbsent(followerId);
    }

    public void addFollowing(String userId) {
        following.addIfAbsent(userId);
    }

    public void removeFollower(String followerId) {
        followers.remove(followerId);
    }

    public void removeFollowing(String userId) {
        following.remove(userId);
    }

    public void addPost(Post post) {
        posts.addFirst(post); // newest first
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public List<String> getFollowers() { return followers; }
    public List<String> getFollowing() { return following; }
    public ConcurrentLinkedDeque<Post> getPosts() { return posts; }
    public int getPostCount() { return posts.size(); }

    @Override
    public String toString() {
        return userId + " (" + name + ") [" + posts.size() + " posts, " + followers.size() + " followers]";
    }
}
