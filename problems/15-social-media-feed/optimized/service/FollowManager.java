/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/FollowManager.java — Follow/unfollow relationship management and social graph queries

import java.util.*;

public class FollowManager {
    private Map<String, Set<String>> following; // HashMap<userId, HashSet> = O(1) lookup + O(1) contains
    private Map<String, Set<String>> followers; // reverse index for fan-out-on-write

    public FollowManager() {
        this.following = new HashMap<>();
        this.followers = new HashMap<>();
    }

    public boolean follow(String followerId, String followeeId) {
        if (followerId.equals(followeeId)) return false;
        following.computeIfAbsent(followerId, k -> new HashSet<>());
        followers.computeIfAbsent(followeeId, k -> new HashSet<>());
        boolean added = following.get(followerId).add(followeeId);
        if (added) followers.get(followeeId).add(followerId);
        return added;
    }

    public boolean unfollow(String followerId, String followeeId) {
        Set<String> followingSet = following.get(followerId);
        if (followingSet == null) return false;
        boolean removed = followingSet.remove(followeeId);
        if (removed) {
            Set<String> followerSet = followers.get(followeeId);
            if (followerSet != null) followerSet.remove(followerId);
        }
        return removed;
    }

    public Set<String> getFollowing(String userId) {
        return following.getOrDefault(userId, Collections.emptySet());
    }

    public Set<String> getFollowers(String userId) {
        return followers.getOrDefault(userId, Collections.emptySet());
    }

    public int getFollowingCount(String userId) { return getFollowing(userId).size(); }
    public int getFollowerCount(String userId) { return getFollowers(userId).size(); }
}
