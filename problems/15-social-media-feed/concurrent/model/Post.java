/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Post.java — Immutable post with unique ID

import java.util.concurrent.atomic.AtomicLong;

class Post {
    private static final AtomicLong ID_GEN = new AtomicLong(1); // AtomicLong = thread-safe unique ID generator

    private final long postId;       // final = immutable after construction; safe to share
    private final String authorId;   // final = never changes; safe to read from any thread
    private final String content;    // final = immutable; post content never changes
    private final long timestamp;    // final = creation time frozen; used for feed ordering

    public Post(String authorId, String content) {
        this.postId = ID_GEN.getAndIncrement();
        this.authorId = authorId;
        this.content = content;
        this.timestamp = System.nanoTime();
    }

    public long getPostId() { return postId; }
    public String getAuthorId() { return authorId; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Post)) return false;
        return postId == ((Post) o).postId;
    }

    @Override
    public int hashCode() { return Long.hashCode(postId); }

    @Override
    public String toString() {
        return "Post#" + postId + " by " + authorId + ": " + content;
    }
}
