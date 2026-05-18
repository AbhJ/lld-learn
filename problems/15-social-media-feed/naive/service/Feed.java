/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Feed.java — Feed generator collecting posts and applying ranking strategy

import java.util.*;

public class Feed {
    private User user;              // private = whose feed to generate
    private FeedStrategy strategy;  // private = strategy pattern for ranking posts

    public Feed(User user, FeedStrategy strategy) {
        this.user = user;
        this.strategy = strategy;
    }

    public void setStrategy(FeedStrategy strategy) {
        this.strategy = strategy;
    }

    public List<Post> generate(List<Post> allPosts, Set<String> following) {
        List<Post> relevantPosts = new ArrayList<>();
        for (Post post : allPosts) {
            if (following.contains(post.getAuthorId())) {
                relevantPosts.add(post);
            }
        }
        return strategy.rankPosts(relevantPosts);
    }

    public FeedIterator iterate(List<Post> posts, int pageSize) {
        return new FeedIterator(posts, pageSize);
    }

    public String getStrategyName() { return strategy.getName(); }
}
