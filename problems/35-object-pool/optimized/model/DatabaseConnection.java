/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/DatabaseConnection.java — Example expensive-to-create pooled resource
public class DatabaseConnection {
    private static int counter = 0;   // static = shared counter; tracks total connections created
    private final String id;          // final = unique ID assigned once at creation
    private boolean open;             // mutable state = connection can be closed/invalidated

    public DatabaseConnection() {
        this.id = "DBConn-" + (++counter);
        this.open = true;
    }

    public String executeQuery(String sql) {
        if (!open) throw new IllegalStateException("Connection closed: " + id);
        return "Result[" + id + "]: " + sql;
    }

    public boolean isOpen() { return open; }
    public void close() { open = false; }
    public String getId() { return id; }
    @Override public String toString() { return id; }
    public static void resetCounter() { counter = 0; }
}
