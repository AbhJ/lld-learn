/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/OrderHistory.java — Maintains order history timeline
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderHistory {
    private List<HistoryEntry> entries; // private = history only modified through addEntry()

    public OrderHistory() { this.entries = new ArrayList<>(); }

    public void addEntry(String status, String description) {
        entries.add(new HistoryEntry(status, description, LocalDateTime.now()));
    }

    public void printTimeline() {
        for (HistoryEntry entry : entries) {
            System.out.println("  " + entry.getStatus() + " -> " + entry.getDescription());
        }
    }

    public static class HistoryEntry { // static = no reference to outer class; stands alone
        private String status;          // private = encapsulates the state name
        private String description;     // private = encapsulates what happened
        private LocalDateTime timestamp; // private = when this entry was created

        public HistoryEntry(String status, String description, LocalDateTime timestamp) {
            this.status = status; this.description = description; this.timestamp = timestamp;
        }
        public String getStatus() { return status; }
        public String getDescription() { return description; }
    }
}
