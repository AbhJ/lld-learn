/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/ObjectFactory.java — Creates and destroys pooled objects
// DESIGN PATTERN: Factory
public interface ObjectFactory<T> { // interface + generic = factory for any pooled resource type
    T create();
    void destroy(T object);
    boolean validate(T object);
}

class DatabaseConnectionFactory implements ObjectFactory<DatabaseConnection> { // implements = concrete factory for DB connections
    @Override public DatabaseConnection create() {
        DatabaseConnection c = new DatabaseConnection();
        System.out.println("    [Factory] Created: " + c.getId());
        return c;
    }
    @Override public void destroy(DatabaseConnection c) { c.close(); }
    @Override public boolean validate(DatabaseConnection c) { return c != null && c.isOpen(); }
}
