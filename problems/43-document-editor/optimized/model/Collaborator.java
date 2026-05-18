/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Collaborator.java — Represents a user collaborating on the document
public class Collaborator {
    private String userId;
    private String name;
    private Cursor cursor;
    private boolean active;

    public Collaborator(String userId, String name) {
        this.userId = userId; this.name = name; this.cursor = new Cursor(userId); this.active = true;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public Cursor getCursor() { return cursor; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() { return name + " (cursor at " + cursor.getPosition() + ")"; }
}
