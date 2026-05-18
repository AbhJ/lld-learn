/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/User.java — Represents a user who can book rooms
public class User {
    private String id;          // private = user ID encapsulated
    private String name;        // private = user's display name
    private String department;  // private = which department user belongs to
    private int floor;          // private = user's home floor (for proximity matching)

    public User(String id, String name, String department, int floor) {
        this.id = id; this.name = name; this.department = department; this.floor = floor;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getFloor() { return floor; }

    @Override public String toString() { return name + " (" + department + ")"; }
}
