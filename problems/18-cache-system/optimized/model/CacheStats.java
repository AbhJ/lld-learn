/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/CacheStats.java — Cache performance metrics (hits, misses, evictions)

public class CacheStats {
    private int hits;
    private int misses;
    private int evictions;
    private int puts;

    public CacheStats() {
        this.hits = 0;
        this.misses = 0;
        this.evictions = 0;
        this.puts = 0;
    }

    public void recordHit() { hits++; }
    public void recordMiss() { misses++; }
    public void recordEviction() { evictions++; }
    public void recordPut() { puts++; }

    public int getHits() { return hits; }
    public int getMisses() { return misses; }
    public int getEvictions() { return evictions; }
    public int getPuts() { return puts; }
    public int getTotalRequests() { return hits + misses; }

    public double getHitRate() {
        int total = getTotalRequests();
        if (total == 0) return 0.0;
        return (double) hits / total * 100;
    }

    @Override
    public String toString() {
        return String.format("CacheStats[hits=%d, misses=%d, evictions=%d, puts=%d, hitRate=%.1f%%]",
                hits, misses, evictions, puts, getHitRate());
    }
}
