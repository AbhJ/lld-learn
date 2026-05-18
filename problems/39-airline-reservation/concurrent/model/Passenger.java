/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Passenger.java — Simple passenger representation

public class Passenger {
    private final String name;             // final = safe publication; name visible to all threads
    private final int id;                  // final = safe publication; unique ID visible to all threads

    public Passenger(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() { return name; }
    public int getId() { return id; }

    @Override
    public String toString() {
        return name + " (#" + id + ")";
    }
}
