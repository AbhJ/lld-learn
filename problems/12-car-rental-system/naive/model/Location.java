/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Location.java — Rental branch location with address

public class Location {
    private String locationId;      // private = encapsulated unique identifier
    private String name;            // private = location name; accessed via getter
    private String address;         // private = address hidden; accessed via getter

    public Location(String locationId, String name, String address) {
        this.locationId = locationId;
        this.name = name;
        this.address = address;
    }

    public String getLocationId() { return locationId; }
    public String getName() { return name; }
    public String getAddress() { return address; }

    @Override
    public String toString() {
        return name + " (" + address + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        return locationId.equals(((Location) o).locationId);
    }

    @Override
    public int hashCode() { return locationId.hashCode(); }
}
