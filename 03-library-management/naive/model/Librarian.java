/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Librarian.java — Admin user who manages books and members

class Librarian {
    private String name;              // private = only this class accesses directly
    private String employeeId;        // private = encapsulated identity

    public Librarian(String name, String employeeId) {
        this.name = name;
        this.employeeId = employeeId;
    }

    public String getName() { return name; }
    public String getEmployeeId() { return employeeId; }

    @Override
    public String toString() {
        return "Librarian: " + name + " (" + employeeId + ")";
    }
}
