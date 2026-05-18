/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Guest.java — Hotel guest with personal details

class Guest {
    private static int counter = 0;
    private String guestId;
    private String name;
    private String email;
    private String phone;

    public Guest(String name, String email, String phone) {
        this.guestId = "G-" + (++counter);
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getGuestId() { return guestId; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    @Override
    public String toString() { return name + " (" + guestId + ")"; }
    public static void resetCounter() { counter = 0; }
}
