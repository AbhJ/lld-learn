/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Customer.java — Rental customer with license info and contact details

public class Customer {
    private String customerId;      // private = only this class can access; encapsulates data
    private String name;            // private = hidden from outside; must use getter to read
    private String email;           // private = contact info encapsulated
    private String phone;           // private = contact info encapsulated
    private String driverLicense;   // private = sensitive info; only exposed via getter

    public Customer(String customerId, String name, String email, String phone, String driverLicense) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.driverLicense = driverLicense;
    }

    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDriverLicense() { return driverLicense; }

    public void notify(String message) {
        System.out.println("[Notification to " + name + "] " + message);
    }

    @Override
    public String toString() {
        return String.format("Customer[%s, %s]", customerId, name);
    }
}
