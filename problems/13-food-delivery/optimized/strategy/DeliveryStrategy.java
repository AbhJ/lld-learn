/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/DeliveryStrategy.java — Spatial grid-based agent lookup for O(1) nearest-agent retrieval
// DESIGN PATTERN: Strategy

import java.util.*;

public interface DeliveryStrategy { // interface = swappable agent selection algorithm
    DeliveryAgent selectAgent(List<DeliveryAgent> agents, Restaurant restaurant);
    String getName();
}

/**
 * Optimized: Grid-based spatial index for O(1) nearest agent lookup.
 * Divides the map into grid cells; agents register in cells by location.
 * Searching starts from the restaurant's cell and expands outward in rings.
 */
class SpatialGridStrategy implements DeliveryStrategy {
    private double cellSize;                        // grid cell dimension in degrees
    private Map<String, List<DeliveryAgent>> grid;  // HashMap = O(1) cell lookup by "x:y" key

    public SpatialGridStrategy(double cellSize) {
        this.cellSize = cellSize;
        this.grid = new HashMap<>();
    }

    private String cellKey(double lat, double lng) {
        int cellX = (int) Math.floor(lat / cellSize);
        int cellY = (int) Math.floor(lng / cellSize);
        return cellX + ":" + cellY;
    }

    public void indexAgents(List<DeliveryAgent> agents) {
        grid.clear();
        for (DeliveryAgent agent : agents) {
            if (!agent.isAvailable()) continue;
            String key = cellKey(agent.getLatitude(), agent.getLongitude());
            grid.computeIfAbsent(key, k -> new ArrayList<>()).add(agent);
        }
    }

    @Override
    public DeliveryAgent selectAgent(List<DeliveryAgent> agents, Restaurant restaurant) {
        indexAgents(agents);

        double lat = restaurant.getLatitude();
        double lng = restaurant.getLongitude();
        int centerX = (int) Math.floor(lat / cellSize);
        int centerY = (int) Math.floor(lng / cellSize);

        // Expand search in rings from center cell
        for (int ring = 0; ring <= 5; ring++) {
            DeliveryAgent best = null;
            double bestDist = Double.MAX_VALUE;

            for (int dx = -ring; dx <= ring; dx++) {
                for (int dy = -ring; dy <= ring; dy++) {
                    if (Math.abs(dx) != ring && Math.abs(dy) != ring) continue; // Only ring border
                    String key = (centerX + dx) + ":" + (centerY + dy);
                    List<DeliveryAgent> cellAgents = grid.get(key);
                    if (cellAgents == null) continue;

                    for (DeliveryAgent agent : cellAgents) {
                        if (!agent.isAvailable()) continue;
                        double dist = agent.distanceTo(lat, lng);
                        if (dist < bestDist) {
                            bestDist = dist;
                            best = agent;
                        }
                    }
                }
            }
            if (best != null) return best;
        }
        return null;
    }

    @Override
    public String getName() { return "Spatial Grid (O(1) cell lookup)"; }
}

/**
 * Priority queue based dispatch: agents sorted by composite score
 * (distance + load factor) for balanced assignment.
 */
class PriorityDispatchStrategy implements DeliveryStrategy { // PriorityQueue-based scoring
    @Override
    public DeliveryAgent selectAgent(List<DeliveryAgent> agents, Restaurant restaurant) {
        PriorityQueue<DeliveryAgent> pq = new PriorityQueue<>((a, b) -> {
            double scoreA = a.distanceTo(restaurant.getLatitude(), restaurant.getLongitude())
                           + a.getActiveOrders() * 2.0;
            double scoreB = b.distanceTo(restaurant.getLatitude(), restaurant.getLongitude())
                           + b.getActiveOrders() * 2.0;
            return Double.compare(scoreA, scoreB);
        });

        for (DeliveryAgent agent : agents) {
            if (agent.isAvailable()) pq.offer(agent);
        }
        return pq.poll();
    }

    @Override
    public String getName() { return "Priority Dispatch (distance + load)"; }
}
