/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/CancelOrderCommand.java — Command that cancels a pre-existing order

class CancelOrderCommand implements OrderCommand {
    private final FoodDeliverySystem system;
    private final Order order;
    private OrderState previousState;               // snapshot for undo()

    public CancelOrderCommand(FoodDeliverySystem system, Order order) {
        this.system = system;
        this.order = order;
    }

    @Override
    public boolean execute() {
        previousState = order.getState();
        return system.cancelOrder(order);
    }

    @Override
    public boolean undo() {
        // Best-effort: restore prior state. Real systems would use compensating actions.
        if (previousState == null) return false;
        return order.transition(previousState);
    }

    @Override
    public String name() { return "CancelOrder"; }
}
