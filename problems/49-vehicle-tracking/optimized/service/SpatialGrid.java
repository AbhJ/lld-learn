/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/SpatialGrid.java — Spatial grid index for O(1) geofence candidate lookup
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpatialGrid {
    // WHY: Spatial grid partitions space into cells, so geofence check only tests
    // fences in the same cell — O(1) average case instead of O(n) scan of all fences
    private Map<Long, List<GeoFence>> grid; // HashMap<cellKey, fences> = O(1) lookup by grid cell
    private double cellSizeDegrees;        // cell size in degrees; maps meters to lat/lon grid

    public SpatialGrid(double cellSizeMeters) {
        this.grid = new HashMap<>();
        this.cellSizeDegrees = cellSizeMeters / 111320.0;
    }

    private long cellKey(double lat, double lon) {
        long latCell = (long)(lat / cellSizeDegrees);
        long lonCell = (long)(lon / cellSizeDegrees);
        return latCell * 1000000L + lonCell;
    }

    public void addFence(GeoFence fence) {
        // Add to all cells the fence's bounding box overlaps
        double minLat = fence.getMinLat();
        double maxLat = fence.getMaxLat();
        double minLon = fence.getMinLon();
        double maxLon = fence.getMaxLon();

        for (double lat = minLat; lat <= maxLat; lat += cellSizeDegrees) {
            for (double lon = minLon; lon <= maxLon; lon += cellSizeDegrees) {
                long key = cellKey(lat, lon);
                grid.computeIfAbsent(key, k -> new ArrayList<>()).add(fence);
            }
        }
    }

    // WHY: O(1) — only returns fences whose bounding box intersects this cell
    public List<GeoFence> getCandidateFences(Location location) {
        long key = cellKey(location.getLatitude(), location.getLongitude());
        return grid.getOrDefault(key, new ArrayList<>());
    }
}
