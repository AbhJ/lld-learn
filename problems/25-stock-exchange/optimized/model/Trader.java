/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Trader.java — Market participant
import java.util.ArrayList;
import java.util.List;
public class Trader {
    private String id;
    private String name;
    private List<String> notifications;                   // ArrayList = append-only trade notification log
    public Trader(String id, String name) { this.id = id; this.name = name; this.notifications = new ArrayList<>(); }
    public String getId() { return id; }
    public String getName() { return name; }
    public void notify(String msg) { notifications.add(msg); System.out.println("  [" + name + "] " + msg); }
    @Override public String toString() { return name; }
}
