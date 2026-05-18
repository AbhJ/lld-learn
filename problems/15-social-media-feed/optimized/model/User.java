/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/User.java — Social media user with profile data and posts

import java.util.*;

public class User {
    private String userId;          // used as key in HashMap and FollowManager sets
    private String name;            // display name; cached in posts to avoid lookups
    private String bio;             // profile bio
    private List<Post> posts;       // ArrayList = O(1) append; user's own posts

    public User(String userId, String name, String bio) {
        this.userId = userId;
        this.name = name;
        this.bio = bio;
        this.posts = new ArrayList<>();
    }

    public Post createPost(String content) {
        Post post = new Post(userId, name, content);
        posts.add(post);
        return post;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getBio() { return bio; }
    public List<Post> getPosts() { return Collections.unmodifiableList(posts); }

    @Override
    public String toString() { return String.format("@%s (%s)", name, userId); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        return userId.equals(((User) o).userId);
    }

    @Override
    public int hashCode() { return userId.hashCode(); }
}
