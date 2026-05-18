/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Customer.java — Delivery customer with address and contact info

public class Customer {
    private String customerId;      // private = only this class can access; encapsulates data
    private String name;            // private = hidden from outside; must use getter to read
    private String email;           // private = contact info encapsulated
    private String phone;           // private = contact info encapsulated
    private String address;         // private = delivery address; accessed via getter
    private double latitude;        // private = GPS coordinate encapsulated
    private double longitude;       // private = GPS coordinate encapsulated

    public Customer(String customerId, String name, String email, String phone,
                    String address, double lat, double lng) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.latitude = lat;
        this.longitude = lng;
    }

    public void onOrderUpdate(Order order) {
        System.out.println("  [Notification to " + name + "] Order " + order.getOrderId() +
                " status: " + order.getState());
    }

    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    @Override
    public String toString() { return name; }
}
