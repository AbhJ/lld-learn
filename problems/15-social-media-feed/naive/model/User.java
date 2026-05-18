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
    private String userId;          // private = encapsulated unique user identifier
    private String name;            // private = display name; accessed via getter
    private String bio;             // private = profile bio; accessed via getter
    private List<Post> posts;       // private = user's own posts; encapsulated

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
