/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/FeedIterator.java — Paginated iteration over feed posts for infinite scroll

import java.util.*;

public class FeedIterator {
    private List<Post> posts;       // private = the ranked feed posts to paginate
    private int pageSize;           // private = how many posts per page
    private int currentIndex;       // private = tracks scroll position

    public FeedIterator(List<Post> posts, int pageSize) {
        this.posts = posts;
        this.pageSize = pageSize;
        this.currentIndex = 0;
    }

    public boolean hasNextPage() {
        return currentIndex < posts.size();
    }

    public List<Post> nextPage() {
        if (!hasNextPage()) return Collections.emptyList();

        int end = Math.min(currentIndex + pageSize, posts.size());
        List<Post> page = posts.subList(currentIndex, end);
        currentIndex = end;
        return page;
    }

    public int getCurrentPage() {
        return (currentIndex / pageSize);
    }

    public int getTotalPages() {
        return (int) Math.ceil((double) posts.size() / pageSize);
    }

    public void reset() {
        currentIndex = 0;
    }
}
