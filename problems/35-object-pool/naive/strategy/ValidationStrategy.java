/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/ValidationStrategy.java — Determines if a pooled object is still usable
// DESIGN PATTERN: Strategy
public interface ValidationStrategy<T> { // interface = strategy pattern; swappable validation rules
    boolean validate(T object);
}

class ConnectionValidator implements ValidationStrategy<DatabaseConnection> { // implements = concrete validator for DB connections
    @Override
    public boolean validate(DatabaseConnection conn) {
        return conn != null && conn.isOpen();
    }
}
