/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/DeliveryStrategy.java — Interchangeable delivery agent assignment algorithms
// DESIGN PATTERN: Strategy

import java.util.List;

public interface DeliveryStrategy { // interface = contract for agent assignment algorithms
    DeliveryAgent selectAgent(List<DeliveryAgent> agents, Restaurant restaurant);
    String getName();
}

class NearestAgentStrategy implements DeliveryStrategy { // implements = fulfills the interface contract
    @Override
    public DeliveryAgent selectAgent(List<DeliveryAgent> agents, Restaurant restaurant) {
        DeliveryAgent nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (DeliveryAgent agent : agents) {
            if (!agent.isAvailable()) continue;
            double dist = agent.distanceTo(restaurant.getLatitude(), restaurant.getLongitude());
            if (dist < minDistance) {
                minDistance = dist;
                nearest = agent;
            }
        }
        return nearest;
    }

    @Override
    public String getName() { return "Nearest Agent"; }
}

class LeastBusyStrategy implements DeliveryStrategy { // implements = fulfills the interface contract
    @Override
    public DeliveryAgent selectAgent(List<DeliveryAgent> agents, Restaurant restaurant) {
        DeliveryAgent leastBusy = null;
        int minOrders = Integer.MAX_VALUE;

        for (DeliveryAgent agent : agents) {
            if (!agent.isAvailable()) continue;
            if (agent.getActiveOrders() < minOrders) {
                minOrders = agent.getActiveOrders();
                leastBusy = agent;
            }
        }
        return leastBusy;
    }

    @Override
    public String getName() { return "Least Busy"; }
}
