/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Player.java — Player with position tracking for concurrent game

package model;

public class Player {
    private final int id;               // final = id never changes; safe publication to all threads
    private final String name;          // final = name set once; visible to all threads after construction
    private volatile int position;      // volatile = position visible to all threads immediately when written

    public Player(int id, String name) {
        this.id = id;
        this.name = name;
        this.position = 0;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    @Override
    public String toString() {
        return name + "(pos=" + position + ")";
    }
}
