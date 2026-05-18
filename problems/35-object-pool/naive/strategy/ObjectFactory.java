/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/ObjectFactory.java — Creates and destroys pooled objects
// DESIGN PATTERN: Factory
public interface ObjectFactory<T> { // interface + generic = factory contract for any pooled type
    T create();
    void destroy(T object);
}

class DatabaseConnectionFactory implements ObjectFactory<DatabaseConnection> { // implements = provides concrete creation logic
    @Override
    public DatabaseConnection create() {
        DatabaseConnection conn = new DatabaseConnection();
        System.out.println("    [Factory] Created: " + conn.getId());
        return conn;
    }

    @Override
    public void destroy(DatabaseConnection conn) {
        conn.close();
    }
}
