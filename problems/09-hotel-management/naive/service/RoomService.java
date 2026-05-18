/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/RoomService.java — Manages in-room service orders

import java.util.ArrayList;
import java.util.List;

class ServiceItem {
    private String name;                // private = item name encapsulated
    private double price;               // private = price encapsulated; access via getter

    public ServiceItem(String name, double price) { this.name = name; this.price = price; }
    public String getName() { return name; }
    public double getPrice() { return price; }
}

class RoomService {
    private List<ServiceItem> orders;   // private = order list managed via addOrder()
    private String roomNumber;          // private = associates services with a room

    public RoomService(String roomNumber) {
        this.roomNumber = roomNumber;
        this.orders = new ArrayList<>();
    }

    public void addOrder(String itemName, double price) { orders.add(new ServiceItem(itemName, price)); }

    public double getTotal() {
        double total = 0;
        for (ServiceItem item : orders) total += item.getPrice();
        return total;
    }

    public String getRoomNumber() { return roomNumber; }
}
