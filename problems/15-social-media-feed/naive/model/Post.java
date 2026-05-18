/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Post.java — Social media post with content, likes, and comments

import java.time.LocalDateTime;
import java.util.*;

public class Post implements PostComponent {

    @Override
    public String getDisplayContent() {
        return String.format("%s: \"%s\"", authorName, content);
    }

    private String postId;          // private = encapsulated unique post ID
    private String authorId;        // private = who created the post
    private String authorName;      // private = author display name
    private String content;         // private = post text content
    private LocalDateTime createdAt; // private = when post was created; used for sorting
    private Set<String> likes;      // private = Set prevents duplicate likes per user
    private List<Comment> comments; // private = comment list managed internally
    private static int counter = 0; // static = shared counter for unique post IDs

    public Post(String authorId, String authorName, String content) {
        this.postId = "POST-" + (++counter);
        this.authorId = authorId;
        this.authorName = authorName;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.likes = new HashSet<>();
        this.comments = new ArrayList<>();
    }

    // For testing with controlled timestamps
    public Post(String authorId, String authorName, String content, LocalDateTime createdAt) {
        this.postId = "POST-" + (++counter);
        this.authorId = authorId;
        this.authorName = authorName;
        this.content = content;
        this.createdAt = createdAt;
        this.likes = new HashSet<>();
        this.comments = new ArrayList<>();
    }

    public boolean like(String userId) {
        return likes.add(userId);
    }

    public boolean unlike(String userId) {
        return likes.remove(userId);
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public int getLikeCount() { return likes.size(); }
    public int getCommentCount() { return comments.size(); }
    public int getEngagement() { return likes.size() + comments.size() * 2; }

    public String getPostId() { return postId; }
    public String getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Set<String> getLikes() { return Collections.unmodifiableSet(likes); }
    public List<Comment> getComments() { return Collections.unmodifiableList(comments); }

    @Override
    public String toString() {
        return String.format("[%s] %s: \"%s\" (%d likes, %d comments)",
                postId, authorName, content, getLikeCount(), getCommentCount());
    }
}
