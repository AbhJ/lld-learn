/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ConnectionPool.java — Naive: synchronized ArrayList for connection management
import java.util.ArrayList;
import java.util.List;

public class ConnectionPool {
    private PoolConfig config;
    private int connCounter;
    private List<Connection> idleConnections;        // ArrayList = simple list; requires synchronized for thread safety
    private List<Connection> activeConnections;      // ArrayList = tracks borrowed connections
    private boolean shutdown;

    public ConnectionPool(PoolConfig config) {
        this.config = config;
        this.idleConnections = new ArrayList<>();
        this.activeConnections = new ArrayList<>();
        this.connCounter = 0;
        this.shutdown = false;
        for (int i = 0; i < config.getMinSize(); i++) {
            idleConnections.add(createConnection());
        }
    }

    private Connection createConnection() {
        return new Connection("Conn-" + (++connCounter));
    }

    public synchronized Connection borrow() { // synchronized = locks entire pool; only one thread can borrow at a time
        if (shutdown) throw new IllegalStateException("Pool is shut down");

        long deadline = System.currentTimeMillis() + config.getBorrowTimeoutMs();
        while (true) {
            if (!idleConnections.isEmpty()) {
                Connection conn = idleConnections.remove(idleConnections.size() - 1);
                if (conn.isHealthy()) { activeConnections.add(conn); return conn; }
            }
            if (activeConnections.size() + idleConnections.size() < config.getMaxSize()) {
                Connection conn = createConnection();
                activeConnections.add(conn);
                return conn;
            }
            if (System.currentTimeMillis() >= deadline) {
                throw new RuntimeException("Pool exhausted: timeout");
            }
            try { wait(100); } catch (InterruptedException e) { throw new RuntimeException(e); }
        }
    }

    public synchronized void returnConnection(Connection conn) { // synchronized = same lock as borrow; prevents data corruption
        activeConnections.remove(conn);
        if (!shutdown && conn.isHealthy()) idleConnections.add(conn);
        else conn.close();
        notifyAll();
    }

    public synchronized void shutdown() { // synchronized = prevents borrow during shutdown
        shutdown = true;
        for (Connection c : idleConnections) c.close();
        idleConnections.clear();
    }

    public synchronized PoolStats getStats() {
        return new PoolStats(activeConnections.size(), idleConnections.size(),
            activeConnections.size() + idleConnections.size());
    }
}
