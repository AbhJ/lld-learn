/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Customer.java — Delivery customer with address and contact info

public class Customer {
    private String customerId;      // unique customer identifier
    private String name;            // customer display name
    private String email;           // contact info
    private String phone;           // contact info
    private String address;         // delivery address text
    private double latitude;        // GPS lat for distance calculation
    private double longitude;       // GPS lng for distance calculation

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
