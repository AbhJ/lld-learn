/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Warehouse.java — Warehouse with distance for proximity sorting
public class Warehouse implements Comparable<Warehouse> { // implements Comparable = TreeMap sorts by distance
    private final String id;               // final = warehouse ID is permanent
    private final String name;             // final = name never changes
    private final String location;         // final = location is fixed
    private final int distanceKm;          // final = distance from reference point for proximity sort

    public Warehouse(String id, String name, String location, int distanceKm) {
        this.id = id; this.name = name; this.location = location; this.distanceKm = distanceKm;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public int getDistanceKm() { return distanceKm; }

    // WHY Comparable: TreeMap sorts warehouses by distance, so nearest-first
    // fulfillment is just treeMap.firstEntry() — O(log n) insert, O(1) nearest.
    @Override
    public int compareTo(Warehouse other) {
        return Integer.compare(this.distanceKm, other.distanceKm);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Warehouse)) return false;
        return id.equals(((Warehouse) o).id);
    }
    @Override public int hashCode() { return id.hashCode(); }
    @Override public String toString() { return name + " (" + location + ", " + distanceKm + "km)"; }
}
