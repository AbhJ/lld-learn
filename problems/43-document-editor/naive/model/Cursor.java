/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Cursor.java — Tracks the current editing position within the document
public class Cursor {
    private int position;                     // private = moved only through moveTo/moveBy methods
    private String userId;                    // private = which collaborator owns this cursor

    public Cursor(String userId) { this.userId = userId; this.position = 0; }

    public int getPosition() { return position; }
    public String getUserId() { return userId; }

    public void moveTo(int position) { if (position >= 0) this.position = position; }
    public void moveBy(int offset) { int newPos = position + offset; if (newPos >= 0) this.position = newPos; }

    @Override
    public String toString() { return "Cursor[user=" + userId + ", pos=" + position + "]"; }
}
