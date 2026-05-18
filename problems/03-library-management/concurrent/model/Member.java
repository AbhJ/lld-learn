/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Member.java — Library member identified by name

package model;

public class Member {
    private final String name;        // final = immutable; safe to read from any thread
    private final int id;             // final = immutable identity; no sync needed

    public Member(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() { return name; }
    public int getId() { return id; }

    @Override
    public String toString() {
        return "Member(" + id + ", " + name + ")";
    }
}
