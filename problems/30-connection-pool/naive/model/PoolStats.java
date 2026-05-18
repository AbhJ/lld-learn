/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/PoolStats.java — Pool utilization metrics
public class PoolStats {
    private int active;
    private int idle;
    private int total;

    public PoolStats(int active, int idle, int total) {
        this.active = active; this.idle = idle; this.total = total;
    }
    public int getActive() { return active; }
    public int getIdle() { return idle; }
    public int getTotal() { return total; }
    @Override public String toString() { return String.format("Active=%d, Idle=%d, Total=%d", active, idle, total); }
}
