/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Connection.java — A database connection with health state
public class Connection {
    private String id;              // private = encapsulated identifier
    private boolean open;           // private = tracks connection lifecycle state
    private boolean healthy;        // private = mutable health flag; set via setHealthy()
    private int usageCount;         // private = internal counter for how many times used

    public Connection(String id) {
        this.id = id; this.open = true; this.healthy = true; this.usageCount = 0;
    }

    public String getId() { return id; }
    public boolean isOpen() { return open; }
    public boolean isHealthy() { return healthy; }
    public int getUsageCount() { return usageCount; }
    public void setHealthy(boolean healthy) { this.healthy = healthy; }

    public String execute(String query) {
        if (!open) throw new IllegalStateException("Connection closed: " + id);
        if (!healthy) throw new IllegalStateException("Connection unhealthy: " + id);
        this.usageCount++;
        return "Result of '" + query + "' on " + id;
    }

    public void close() { this.open = false; }
    @Override public String toString() { return id; }
}
