/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Passenger.java — Traveler with personal details
public class Passenger {
    private final String name;             // final = passenger name is immutable
    private final String email;            // final = email is immutable

    public Passenger(String name, String email) { this.name = name; this.email = email; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    @Override public String toString() { return name; }
}
