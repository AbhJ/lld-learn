/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/AssignDriverCommand.java — Command that picks an agent and dispatches the order

class AssignDriverCommand implements OrderCommand {
    private final FoodDeliverySystem system;
    private final Order order;
    private DeliveryAgent assigned;                 // captured for undo()

    public AssignDriverCommand(FoodDeliverySystem system, Order order) {
        this.system = system;
        this.order = order;
    }

    @Override
    public boolean execute() {
        boolean ok = system.assignAndDispatch(order);
        if (ok) assigned = order.getAgent();
        return ok;
    }

    @Override
    public boolean undo() {
        // Free up the agent by marking the order cancelled (compensating action).
        if (assigned == null) return false;
        return system.cancelOrder(order);
    }

    @Override
    public String name() { return "AssignDriver"; }
}
