/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/FeedStrategy.java — Interchangeable feed ranking algorithms (chronological, engagement)
// DESIGN PATTERN: Strategy

import java.util.*;

public interface FeedStrategy { // interface = swappable feed ranking algorithm
    List<Post> rankPosts(List<Post> posts);
    String getName();
}

class ChronologicalFeed implements FeedStrategy {
    @Override
    public List<Post> rankPosts(List<Post> posts) {
        List<Post> sorted = new ArrayList<>(posts);
        sorted.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return sorted;
    }

    @Override
    public String getName() { return "Chronological"; }
}

class RankedFeed implements FeedStrategy {
    @Override
    public List<Post> rankPosts(List<Post> posts) {
        List<Post> sorted = new ArrayList<>(posts);
        // Rank by engagement (likes + 2*comments), break ties by recency
        sorted.sort((a, b) -> {
            int engDiff = b.getEngagement() - a.getEngagement();
            if (engDiff != 0) return engDiff;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });
        return sorted;
    }

    @Override
    public String getName() { return "Engagement-Ranked"; }
}
