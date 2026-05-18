/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/PlaceOrderCommand.java — Command that places a new order via the system

import java.util.Map;

class PlaceOrderCommand implements OrderCommand {
    private final FoodDeliverySystem system;        // facade the command operates on
    private final Customer customer;
    private final Restaurant restaurant;
    private final Map<MenuItem, Integer> items;
    private Order placedOrder;                      // populated after execute() for undo

    public PlaceOrderCommand(FoodDeliverySystem system, Customer customer,
                             Restaurant restaurant, Map<MenuItem, Integer> items) {
        this.system = system;
        this.customer = customer;
        this.restaurant = restaurant;
        this.items = items;
    }

    @Override
    public boolean execute() {
        placedOrder = system.placeOrder(customer, restaurant, items);
        return placedOrder != null;
    }

    @Override
    public boolean undo() {
        // Reverting a placed order = cancel it through the system.
        return placedOrder != null && system.cancelOrder(placedOrder);
    }

    public Order getPlacedOrder() { return placedOrder; }

    @Override
    public String name() { return "PlaceOrder"; }
}
