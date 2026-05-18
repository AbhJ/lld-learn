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

    private String postId;          // unique post identifier
    private String authorId;        // links post to author for feed fan-out
    private String authorName;      // cached for display without extra lookup
    private String content;         // post text body
    private LocalDateTime createdAt; // timestamp; sort key for chronological feed
    private Set<String> likes;      // HashSet = O(1) add/remove; prevents duplicate likes
    private List<Comment> comments; // ArrayList = O(1) append for comments
    private static int counter = 0; // shared ID generator across all posts

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
