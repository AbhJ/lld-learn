/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Bidder.java — Auction participant with notifications
import java.util.ArrayList;
import java.util.List;

public class Bidder {
    private String id;
    private String name;
    private List<String> notifications;                   // ArrayList = append-only notification log

    public Bidder(String id, String name) {
        this.id = id; this.name = name; this.notifications = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void notify(String message) { notifications.add(message); System.out.println("  [" + name + "] " + message); }

    @Override public boolean equals(Object o) { return o instanceof Bidder && id.equals(((Bidder) o).id); }
    @Override public int hashCode() { return id.hashCode(); }
    @Override public String toString() { return name; }
}
