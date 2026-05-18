/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Comment.java — Comment on a post with author and content

import java.time.LocalDateTime;

public class Comment {
    private String commentId;       // private = encapsulated unique comment ID
    private String authorId;        // private = who wrote the comment
    private String authorName;      // private = commenter display name
    private String content;         // private = comment text
    private LocalDateTime createdAt; // private = when comment was posted
    private static int counter = 0; // static = shared counter for unique comment IDs

    public Comment(String authorId, String authorName, String content) {
        this.commentId = "CMT-" + (++counter);
        this.authorId = authorId;
        this.authorName = authorName;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public String getCommentId() { return commentId; }
    public String getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return String.format("  > %s: \"%s\"", authorName, content);
    }
}
